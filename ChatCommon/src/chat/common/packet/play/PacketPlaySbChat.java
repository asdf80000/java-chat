package chat.common.packet.play;

import chat.common.listener.PacketPlaySbListener;
import chat.common.main.Utils;
import chat.common.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 
 * @see PacketPlaySbStart
 * @author User
 *
 */
public class PacketPlaySbChat implements Packet<PacketPlaySbListener> {
	public byte[] encText;

	public PacketPlaySbChat() {

	}

	@Override
	public void decode(ByteBuf buf) {
		encText = Utils.getByteArray(buf);
	}

	@Override
	public void encode(ByteBuf buf) {
		Utils.writeByteArray(buf, encText);
	}

	@Override
	public void process(PacketPlaySbListener listener) {
		listener.process(this);
	}

}
