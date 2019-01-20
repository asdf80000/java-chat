package chat.common.packet.play;

import chat.common.listener.PacketPlayCbListener;
import chat.common.main.Utils;
import chat.common.packet.Packet;
import io.netty.buffer.ByteBuf;

public class PacketPlayCbMatchId implements Packet<PacketPlayCbListener> {
	public String newId = "";

	@Override
	public void decode(ByteBuf buf) {
		newId = Utils.getString(buf);
	}

	@Override
	public void encode(ByteBuf buf) {
		Utils.writeString(buf, newId);
	}

	@Override
	public void process(PacketPlayCbListener listener) {
		listener.process(this);
	}

}
