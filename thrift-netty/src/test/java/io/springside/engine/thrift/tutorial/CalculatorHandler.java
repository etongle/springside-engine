package io.springside.engine.thrift.tutorial;

import org.apache.thrift.TException;

public class CalculatorHandler implements Calculator.Iface {

	@Override
	public void ping() throws TException {
		// TODO Auto-generated method stub

	}

	@Override
	public int add(int num1, int num2) throws TException {
		System.out.println("receive " + num1 + " and " + num2);
		return num1 + num2;
	}

	@Override
	public int calculate(int logid, Work w) throws InvalidOperation, TException {
		// TODO Auto-generated method stub
		return 0;
	}

}
