package chat.server.handler;

import chat.common.handler.ProtocolDirection;
import chat.common.main.Utils;
import chat.common.packet.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ChatServerPacketEncoder extends MessageToByteEncoder<Packet<?>> {
	@Override
	protected void encode(ChannelHandlerContext ctx, Packet<?> msg, ByteBuf out) throws Exception {
		int pid = Utils.getChannelAttr(AttributeSaver.state, ctx.channel()).get().m.get(ProtocolDirection.CLIENTBOUND).inverse().get(msg.getClass());
		System.out.println("[Encoder] Server: Writing packet : " + msg.getClass().getSimpleName() + " (0x" + Integer.toHexString(pid) + ") " + Utils.serialize(msg));
		out.writeInt(pid);
		msg.encode(out);
		ctx.flush();
	}
}
