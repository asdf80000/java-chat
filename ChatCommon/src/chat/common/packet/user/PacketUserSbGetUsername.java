package chat.common.packet.user;

import chat.common.listener.PacketUserSbListener;
import chat.common.main.Utils;
import chat.common.packet.Packet;
import io.netty.buffer.ByteBuf;

//@formatter:off
/**
 * Process: > Send isMessage > Send packet > Server send OutSetUsername > End
 * @author User
 *
 */
//@formatter:on
public class PacketUserSbGetUsername implements Packet<PacketUserSbListener> {
	public boolean message = false;
	
	@Override
	public void decode(ByteBuf buf) {
		// Nothing to process. Server should send PacketUserOutSetUsername
		message = buf.readBoolean();
		return;
	}

	@Override
	public void encode(ByteBuf buf) {
		// TODO Auto-generated method stub
		buf.writeBoolean(message);
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
