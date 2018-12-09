package chat.common.packet.play;

import java.security.PublicKey;

import chat.common.listener.PacketPlaySbListener;
import chat.common.main.Utils;
import chat.common.packet.Packet;
import chat.common.work.RSAUtils;
import io.netty.buffer.ByteBuf;

/**
 * @see PacketPlayCbStart
 * @author User
 *
 */
public class PacketPlaySbStart implements Packet<PacketPlaySbListener> {

	public PublicKey cp;
	@Override
	public void decode(ByteBuf buf) {
		cp = RSAUtils.genPublicKey(Utils.getByteArray(buf));
	}

	@Override
	public void encode(ByteBuf buf) {
		Utils.writeByteArray(buf, cp.getEncoded());
	}

	@Override
	public String toString() {
		return Utils.serializePacket(this);
	}

	@Override
	public void process(PacketPlaySbListener listener) {
		listener.process(this);
	}
}
