package chat.common.handler;

import chat.common.main.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ChatCommonPacketPrepender extends MessageToByteEncoder<ByteBuf> {

	@Override
	protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
		msg.markReaderIndex();
		int i = msg.readableBytes();
		if (Utils.log)
			System.out.println("[Prepender] Input from remote: " + i);
		if (msg.readableBytes() < i) {
			msg.resetReaderIndex();
			if (Utils.log)
				System.out.println("[Prepender] No enough bytes");
			return;
		}
		if (Utils.log)
			System.out.println("[Prepender] Writing to processor: " + i);
		out.writeInt(i);
		out.writeBytes(msg.readBytes(i));
	}

}
