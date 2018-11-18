package chat.common.packet.user;

import chat.common.listener.PacketUserSbListener;
import chat.common.main.Utils;
import chat.common.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Nothing to process
 * @author User
 *
 */
public class PacketUserSbStartMatchmake implements Packet<PacketUserSbListener> {
	
	@Override
	public void decode(ByteBuf uf) {
		// Nothing to process
		return;
	}

	@Override
	public void encode(ByteBuf buf) {
		// TODO Auto-generated method stub
		
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
