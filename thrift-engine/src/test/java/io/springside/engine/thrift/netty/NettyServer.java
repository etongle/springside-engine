package io.springside.engine.thrift.netty;

import io.springside.engine.thrift.server.ServerArgs;
import io.springside.engine.thrift.server.TNettyServer;
import io.springside.engine.thrift.tutorial.Calculator;
import io.springside.engine.thrift.tutorial.CalculatorHandler;

import org.apache.thrift.server.TServer;

public class NettyServer {

	public static void main(String[] args) {
		try {
			CalculatorHandler handler = new CalculatorHandler();
			ServerArgs serverArgs = new ServerArgs().port(9090).processor(new Calculator.Processor(handler));

			TNettyServer server = new TNettyServer(serverArgs);
			server.serve();
			server.waitForClose();
		} catch (Exception e) {

		}
	}
}
