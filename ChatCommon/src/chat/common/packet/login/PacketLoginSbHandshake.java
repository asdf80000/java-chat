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
	public String id = "";
	public String pwd = "";

	@Override
	public void decode(ByteBuf uf) {
		id = Utils.getString(uf);
		pwd = Utils.getString(uf);
		return;
	}

	@Override
	public void encode(ByteBuf buf) {
		Utils.writeString(buf, id);
		Utils.writeString(buf, pwd);
	}

	@Override
	public void process(PacketLoginSbListener listener) {
		listener.process(this);
	}

	@Override
	public String toString() {
		return Utils.serializePacket(this);
	}
}
