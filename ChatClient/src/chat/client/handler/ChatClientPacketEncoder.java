package chat.client.handler;

import chat.client.main.ClientMain;
import chat.common.handler.ProtocolDirection;
import chat.common.packet.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ChatClientPacketEncoder extends MessageToByteEncoder<Packet<?>> {
	@Override
	protected void encode(ChannelHandlerContext ctx, Packet<?> msg, ByteBuf out) throws Exception {
		int pid = ClientMain.cs.m.get(ProtocolDirection.SERVERBOUND).inverse().get(msg.getClass());
		System.out.println("[Encoder] Client: Writing packet " + msg.getClass().getSimpleName() + " (0x" + Integer.toHexString(pid) + ") " + msg);
		out.writeInt(pid);
		msg.encode(out);
	}
}
