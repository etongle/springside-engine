package io.springside.engine.thrift.server;

import org.apache.thrift.server.TServer.AbstractServerArgs;

/**
 * Arguments for Netty Server
 */
public class ServerArgs extends AbstractServerArgs<ServerArgs> {

	public int port = -1;

	public int bossThreads = 2;
	public int workerThreads = 0;
	public int userThreads = Runtime.getRuntime().availableProcessors() * 2;

	public int socketTimeoutMills = -1;
	public int shutdownTimeoutMills = 10000;

	public int sendBuff = -1;
	public int recvBuff = -1;

	public ServerArgs() {
		super(null);
	}

	public ServerArgs port(int port) {
		this.port = port;
		return this;
	}

	public ServerArgs bossThreads(int bossThreads) {
		this.bossThreads = bossThreads;
		return this;
	}

	public ServerArgs workerThreads(int workerThreads) {
		this.workerThreads = workerThreads;
		return this;
	}

	public ServerArgs userThreads(int userThreads) {
		this.userThreads = userThreads;
		return this;
	}

	public ServerArgs socketTimeoutMills(int socketTimeoutMills) {
		this.socketTimeoutMills = socketTimeoutMills;
		return this;
	}

	public ServerArgs shutdownTimeoutMills(int shutdownTimeoutMills) {
		this.shutdownTimeoutMills = shutdownTimeoutMills;
		return this;
	}

	public ServerArgs sendBuff(int sendBuff) {
		this.sendBuff = sendBuff;
		return this;
	}

	public ServerArgs recvBuff(int recvBuff) {
		this.recvBuff = recvBuff;
		return this;
	}

	public void validate() {
		if (port < 0) {
			throw new IllegalArgumentException("port " + port + " is wrong.");
		}
	}
}
