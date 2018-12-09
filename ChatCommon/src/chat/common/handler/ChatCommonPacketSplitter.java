package chat.common.handler;

import java.util.List;

import chat.common.main.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class ChatCommonPacketSplitter extends ByteToMessageDecoder {
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		in.markReaderIndex();
		if (in.readableBytes() < 4) {
			if (Utils.log)
				System.out.println("[Splitter] Not enough bytes to read integer!");
			in.resetReaderIndex();
			return;
		}
		int i = in.readInt();
		if (Utils.log) {
			System.out.println("[Splitter] Packet length: " + i);
			System.out.println("[Splitter] Available bytes from client: " + in.readableBytes());
		}
		if (in.readableBytes() < i) {
			if (Utils.log)
				System.out.println("[Splitter] Fragmented packet detected! Waiting for next part of packet...");
			in.resetReaderIndex();
			return;
		}
		if (Utils.log)
			System.out.println("[Splitter] Writing packet!");
		out.add(in.readBytes(i));
	}
}
