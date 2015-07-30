package io.springside.engine.thrift.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class TNettyTransport extends TTransport {

	public static final int DEFAULT_BUFFER_SIZE = 1024;

	private ByteBuf in;
	private ByteBuf out;

	private Channel channel;

	public TNettyTransport(Channel channel, ByteBuf in) {
		this.channel = channel;
		this.in = in;
		out = channel.alloc().buffer(DEFAULT_BUFFER_SIZE);
	}

	@Override
	public int read(byte[] bytes, int offset, int length) throws TTransportException {
		int _read = Math.min(in.readableBytes(), length);
		in.readBytes(bytes, offset, _read);
		return _read;
	}

	@Override
	public void write(byte[] bytes, int offset, int length) throws TTransportException {
		out.writeBytes(bytes, offset, length);
	}

	@Override
	public void open() throws TTransportException {
		// no-op
	}

	@Override
	public boolean isOpen() {
		return channel.isOpen();
	}

	@Override
	public void close() {
		channel.close();
	}

	@Override
	public void flush() throws TTransportException {
		// no-op
	}

	public ByteBuf getFramedOutput() {
		ByteBuf frameSizeBuffer = channel.alloc().buffer(4);
		frameSizeBuffer.writeInt(out.readableBytes());
		return Unpooled.wrappedBuffer(frameSizeBuffer, out);
	}
}
