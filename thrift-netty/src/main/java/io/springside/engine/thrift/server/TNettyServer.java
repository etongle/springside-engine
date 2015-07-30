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
import io.springside.engine.utils.Exceptions;
import io.springside.engine.utils.Threads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.thrift.server.TServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TNettyServer extends TServer {

	private static Logger logger = LoggerFactory.getLogger(TNettyServer.class);

	private ServerArgs args;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	private ExecutorService userThreadPool;

	public TNettyServer(ServerArgs args) {
		super(args);
		args.validate();
		this.args = args;
	}

	@Override
	public void serve() {
		logger.info("Netty Server is starting");
		ServerBootstrap b = configServer();

		try {
			// start server
			ChannelFuture f = b.bind(args.port).sync();
			logger.info("Netty Server started and listening on " + args.port);
			setServing(true);

			Runtime.getRuntime().addShutdownHook(new ShutdownThread());

			// blocking to wait for close
			f.channel().closeFuture().sync();
		} catch (Exception e) {
			logger.error("Exception happen when start server", e);
			throw Exceptions.unchecked(e);
		}
	}

	@Override
	public void stop() {
		logger.info("Netty server is stopping");
		bossGroup.shutdownGracefully();
		Threads.gracefulShutdown(userThreadPool, args.shutdownTimeoutInSecs, args.shutdownTimeoutInSecs,
				TimeUnit.SECONDS);
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
				.childOption(ChannelOption.SO_KEEPALIVE, true)
				.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(createThriftFramedDecoder(), thriftHandler);
					}
				});
		return b;
	}

	private ChannelHandler createThriftFramedDecoder() {
		return new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4);
	}

	class ShutdownThread extends Thread {
		@Override
		public void run() {
			TNettyServer.this.stop();
		}
	}
}
