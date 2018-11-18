package chat.common.packet.match;

import chat.common.listener.PacketMatchSbListener;
import chat.common.main.Utils;
import chat.common.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Nothing to process
 * @author User
 *
 */
public class PacketMatchSbCancelMatchmake implements Packet<PacketMatchSbListener> {
	
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
	public void process(PacketMatchSbListener listener) {
		listener.process(this);
	}
	@Override
	public String toString() {
		return Utils.serializePacket(this);
	}
}
