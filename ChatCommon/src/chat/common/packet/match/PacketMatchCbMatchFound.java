package chat.common.packet.match;

import java.security.PublicKey;

import chat.common.listener.PacketMatchCbListener;
import chat.common.main.Utils;
import chat.common.packet.Packet;
import chat.common.work.RSAUtils;
import io.netty.buffer.ByteBuf;

public class PacketMatchCbMatchFound implements Packet<PacketMatchCbListener> {

	public PublicKey pk;
	@Override
	public void decode(ByteBuf buf) {
		pk = RSAUtils.genPublicKey(Utils.getByteArray(buf));
	}

	@Override
	public void encode(ByteBuf buf) {
		Utils.writeByteArray(buf, pk.getEncoded());
	}

	@Override
	public void process(PacketMatchCbListener listener) {
		listener.process(this);
	}
}
