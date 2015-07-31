package io.springside.engine.thrift.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class ThriftFrameedDecoder extends ByteToMessageDecoder {

	private int LENGTH_FIELD_LENGTH = 4;

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if (in.readableBytes() < LENGTH_FIELD_LENGTH) {
			return;
		}

		int frameLength = in.readInt();

		if (in.readableBytes() < frameLength) {
			return;
		}

		ByteBuf frame = in.slice(LENGTH_FIELD_LENGTH, frameLength).retain();
		in.readerIndex(LENGTH_FIELD_LENGTH + frameLength);
		out.add(frame);
	}
}
