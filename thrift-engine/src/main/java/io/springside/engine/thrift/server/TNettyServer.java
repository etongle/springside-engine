package io.springside.engine.thrift.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.springside.engine.thrift.utils.Exceptions;
import io.springside.engine.thrift.utils.Threads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.thrift.server.TServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thrift NIO Server base on Netty.
 * 
 * @author calvin
 */
public class TNettyServer extends TServer {

	private static Logger logger = LoggerFactory.getLogger(TNettyServer.class);

	private ServerArgs args;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	private ExecutorService userThreadPool;
	private ChannelFuture f;

	public TNettyServer(ServerArgs args) {
		super(args);
		this.args = args;
	}

	@Override
	public void serve() {
		logger.info("Netty Server is starting");

		args.validate();

		ServerBootstrap b = configServer();

		try {
			// start server
			f = b.bind(args.port).sync();
			logger.info("Netty Server started and listening on " + args.port);
			setServing(true);

			// register shutown hook
			Runtime.getRuntime().addShutdownHook(new ShutdownThread());

		} catch (Exception e) {
			logger.error("Exception happen when start server", e);
			throw Exceptions.unchecked(e);
		}
	}

	/**
	 * blocking to wait for close.
	 */
	public void waitForClose() throws InterruptedException {
		f.channel().closeFuture().sync();
	}

	@Override
	public void stop() {
		logger.info("Netty server is stopping");

		bossGroup.shutdownGracefully();
		Threads.gracefulShutdown(userThreadPool, args.shutdownTimeoutMills, args.shutdownTimeoutMills, TimeUnit.SECONDS);
		workerGroup.shutdownGracefully();

		logger.info("Netty server stoped");
	}

	private ServerBootstrap configServer() {
		bossGroup = new NioEventLoopGroup(args.bossThreads, new DefaultThreadFactory("NettyBossGroup", true));
		workerGroup = new NioEventLoopGroup(args.workerThreads, new DefaultThreadFactory("NettyWorkerGroup", true));
		userThreadPool = Executors.newFixedThreadPool(args.userThreads, new DefaultThreadFactory("UserThreads", true));

		final ThriftHandler thriftHandler = new ThriftHandler(this.processorFactory_, this.inputProtocolFactory_,
				this.outputProtocolFactory_, userThreadPool);

		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
				.childOption(ChannelOption.SO_REUSEADDR, true).childOption(ChannelOption.SO_KEEPALIVE, true)
				.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

		if (args.socketTimeoutMills > 0) {
			b.childOption(ChannelOption.SO_TIMEOUT, args.socketTimeoutMills);
		}

		if (args.recvBuff > 0) {
			b.childOption(ChannelOption.SO_RCVBUF, args.recvBuff);
		}

		if (args.sendBuff > 0) {
			b.childOption(ChannelOption.SO_SNDBUF, args.sendBuff);
		}

		b.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(createThriftFramedDecoder(), createThriftFramedEncoder(), thriftHandler);
			}
		});

		return b;
	}

	private ChannelHandler createThriftFramedDecoder() {
		return new ThriftFrameedDecoder();
	}

	private ChannelHandler createThriftFramedEncoder() {
		return new ThriftFrameedEncoder();
	}

	class ShutdownThread extends Thread {
		@Override
		public void run() {
			TNettyServer.this.stop();
		}
	}
}
