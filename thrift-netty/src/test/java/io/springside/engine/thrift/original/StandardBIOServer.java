package io.springside.engine.thrift.original;

import io.springside.engine.thrift.tutorial.Calculator;
import io.springside.engine.thrift.tutorial.CalculatorHandler;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;

public class StandardBIOServer {

	public static CalculatorHandler handler;

	public static Calculator.Processor processor;

	public static void main(String[] args) {
		try {
			handler = new CalculatorHandler();
			processor = new Calculator.Processor(handler);

			try {
				TServerTransport serverTransport = new TServerSocket(9090);
				TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));

				System.out.println("Starting the  server...");
				server.serve();
			} catch (TTransportException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (Exception x) {
			x.printStackTrace();
		}
	}
}
