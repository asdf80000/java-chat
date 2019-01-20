package chat.net;

import chat.common.handler.ProtocolDirection;
import chat.common.main.Utils;
import chat.common.packet.Packet;
import chat.main.MainGUI;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ChatClientPacketEncoder extends MessageToByteEncoder<Packet<?>> {
	@Override
	protected void encode(ChannelHandlerContext ctx, Packet<?> msg, ByteBuf out) throws Exception {
		if (Utils.log)
			System.out.println(msg.getClass());
		int pid = MainGUI.currentState.m.get(ProtocolDirection.SERVERBOUND).inverse().get(msg.getClass());
		if (Utils.log)
			System.out.println("[Encoder] Client: Writing packet " + msg.getClass().getSimpleName() + " (0x"
					+ Integer.toHexString(pid) + ") " + Utils.serialize(msg));
		out.writeInt(pid);
		msg.encode(out);
	}
}
