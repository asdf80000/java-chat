package chat.common.packet.login;

import chat.common.listener.PacketLoginCbListener;
import chat.common.main.Utils;
import chat.common.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Process: > Send username > Status to USER > End
 * 
 * @author User
 *
 */
public class PacketLoginCbWelcome implements Packet<PacketLoginCbListener> {

	public String username = "#unknown";

	@Override
	public void decode(ByteBuf uf) {
		// TODO Auto-generated method stub
		username = Utils.getString(uf);
		return;
	}

	@Override
	public void encode(ByteBuf buf) {
		// TODO Auto-generated method stub
		Utils.writeString(buf, username);
	}

	@Override
	public void process(PacketLoginCbListener listener) {
		listener.process(this);
	}

	@Override
	public String toString() {
		return Utils.serializePacket(this);
	}
}
