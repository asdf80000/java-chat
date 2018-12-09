package chat.common.packet.play;

import chat.common.listener.PacketPlaySbListener;
import chat.common.packet.Packet;
import io.netty.buffer.ByteBuf;

public class PacketPlaySbQuitMatch implements Packet<PacketPlaySbListener> {

	@Override
	public void decode(ByteBuf buf) {
		// TODO Auto-generated method stub

	}

	@Override
	public void encode(ByteBuf buf) {
		// TODO Auto-generated method stub

	}

	@Override
	public void process(PacketPlaySbListener listener) {
		listener.process(this);
	}

}
