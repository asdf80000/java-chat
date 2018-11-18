package chat.common.packet.user;

import chat.common.listener.PacketUserCbListener;
import chat.common.main.Utils;
import chat.common.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Process: > Server send username > End
 * @author User
 *
 */
public class PacketUserCbSetUsername implements Packet<PacketUserCbListener> {
	public String username = "#unknown";
	@Override
	public void decode(ByteBuf buf) {
		// TODO Auto-generated method stub
		username = Utils.getString(buf);
		return;
	}

	@Override
	public void encode(ByteBuf buf) {
		// TODO Auto-generated method stub
		Utils.writeString(buf, username);
	}

	@Override
	public void process(PacketUserCbListener listener) {
		// TODO Auto-generated method stub
		listener.process(this);
	}
	@Override
	public String toString() {
		return Utils.serializePacket(this);
	}
}
