package chat.common.packet.login;

import chat.common.listener.PacketLoginSbListener;
import chat.common.main.Utils;
import chat.common.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Process: > Send Username > Server send Welcome > End
 * 
 * @author User
 *
 */
public class PacketLoginSbHandshake implements Packet<PacketLoginSbListener> {
	public String username = "@unknown";

	@Override
	public void decode(ByteBuf uf) {
		// TODO Auto-generated method stub
		this.username = Utils.getString(uf);
		return;
	}

	@Override
	public void encode(ByteBuf buf) {
		// TODO Auto-generated method stub
		Utils.writeString(buf, username);
	}

	@Override
	public void process(PacketLoginSbListener listener) {
		// TODO Auto-generated method stub
		listener.process(this);
	}
	
	@Override
	public String toString() {
		return Utils.serializePacket(this);
	}
}
