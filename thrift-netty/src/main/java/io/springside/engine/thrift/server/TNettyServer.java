package io.springside.engine.thrift.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.thrift.server.TServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TNettyServer extends TServer {

	private static Logger logger = LoggerFactory.getLogger(TNettyServer.class);

	private ServerArgs args;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;

	public TNettyServer(ServerArgs args) {
		super(args);
		args.validate();
		this.args = args;
	}

	@Override
	public void serve() {
		ServerBootstrap b = configServer();

		try {
			ChannelFuture f = b.bind(args.port).sync();
			setServing(true);
			logger.info("Netty Server started and listening on " + args.port);

			f.channel().closeFuture().sync();

		} catch (Exception e) {
			logger.error("Exception happen when start server", e);
		}
	}

	private ServerBootstrap configServer() {
		bossGroup = new NioEventLoopGroup(args.bossThreads, new DefaultThreadFactory("NettyBossGroup", true));
		workerGroup = new NioEventLoopGroup(args.workerThreads, new DefaultThreadFactory("NettyWorkerGroup", true));
		Executor userExecutor = Executors.newFixedThreadPool(args.userThreads);

		final ThriftHandler thriftHandler = new ThriftHandler(this.processorFactory_, this.inputProtocolFactory_,
				this.outputProtocolFactory_, userExecutor);

		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
				.childOption(ChannelOption.SO_KEEPALIVE, true)
				.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4),
								thriftHandler);
					}
				});
		return b;
	}

	@Override
	public void stop() {
		workerGroup.shutdownGracefully();
		bossGroup.shutdownGracefully();
	}
}
