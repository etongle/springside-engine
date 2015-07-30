package io.springside.engine.thrift.server;

import org.apache.thrift.server.TServer.AbstractServerArgs;

public class ServerArgs extends AbstractServerArgs<ServerArgs> {

	public int port = -1;
	public int bossThreads = 2;
	public int workerThreads = 0;
	public int userThreads = Runtime.getRuntime().availableProcessors() * 2;
	public int shutdownTimeoutInSecs = 10;

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

	public void validate() {
		if (port < 0) {
			throw new IllegalArgumentException("port " + port + " is wrong.");
		}
	}
}
