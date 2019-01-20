package chat.net;

import java.io.IOException;
import java.util.List;

import chat.common.handler.ChannelState;
import chat.common.handler.ProtocolDirection;
import chat.common.main.Utils;
import chat.common.packet.Packet;
import chat.main.MainGUI;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class ChatClientPacketDecoder extends ByteToMessageDecoder {
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		// TODO Auto-generated method stub
		int pid = in.readInt();
		ChannelState cs = MainGUI.currentState;
		Class<? extends Packet<?>> c = cs.m.get(ProtocolDirection.CLIENTBOUND).get(pid);
		if(c == null) {
			throw new IOException("Bad packet id " + pid);
		}
		Packet<?> p = c.getDeclaredConstructor().newInstance();
		p.decode(in);
		System.out.println(MainGUI.initializer.handler.currentPacketListener);
		if(Utils.log) System.out.println("[Decoder] Client: Processing packet " + p.getClass().getSimpleName() + " (0x" + Integer.toHexString(pid) + ") " + Utils.serialize(p));
		out.add(p);
	}
}
