package chat.common.packet.match;

import chat.common.listener.PacketMatchCbListener;
import chat.common.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Server to client. Sends after finding match.
 * 
 * @author User
 *
 */
public class PacketMatchCbMatchFound implements Packet<PacketMatchCbListener> {
	@Override
	public void decode(ByteBuf buf) {
	}

	@Override
	public void encode(ByteBuf buf) {
	}

	@Override
	public void process(PacketMatchCbListener listener) {
		listener.process(this);
	}
}
