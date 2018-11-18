package chat.common.packet.user;

import chat.common.listener.PacketUserCbListener;
import chat.common.main.Utils;
import chat.common.packet.Packet;
import io.netty.buffer.ByteBuf;

public class PacketUserCbAnnounce implements Packet<PacketUserCbListener> {

	public String message = "undefined";
	
	@Override
	public void decode(ByteBuf buf) {
		// TODO Auto-generated method stub
		message = Utils.getString(buf);
	}

	@Override
	public void encode(ByteBuf buf) {
		// TODO Auto-generated method stub
		Utils.writeString(buf, message);
	}

	@Override
	public void process(PacketUserCbListener listener) {
		listener.process(this);
	}
	@Override
	public String toString() {
		return Utils.serializePacket(this);
	}
}
