package chat.common.packet.user;

import chat.common.listener.PacketUserSbListener;
import chat.common.main.Utils;
import chat.common.packet.Packet;
import io.netty.buffer.ByteBuf;

// @formatter:off
/**
 * Client send this to server. Change username packet. 
 * Process: > Client send username > End
 * 
 * @author User
 *
 */
// @formatter:on
public class PacketUserSbSetUsername implements Packet<PacketUserSbListener> {
	public String username = "";
	@Override
	public void decode(ByteBuf buf) {
		username = Utils.getString(buf);
		return;
	}

	@Override
	public void encode(ByteBuf buf) {
		Utils.writeString(buf, username);
	}

	@Override
	public void process(PacketUserSbListener listener) {
		// TODO Auto-generated method stub
		listener.process(this);
	}
	@Override
	public String toString() {
		return Utils.serializePacket(this);
	}
}
