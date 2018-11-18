package chat.common.packet.match;

import java.security.PublicKey;

import chat.common.listener.PacketMatchCbListener;
import chat.common.main.Utils;
import chat.common.packet.Packet;
import chat.common.work.RSAUtils;
import io.netty.buffer.ByteBuf;

public class PacketMatchCbMatchFound implements Packet<PacketMatchCbListener> {

	public PublicKey pk;
	public int GUID = 0;
	public String verifyKey = "";
	@Override
	public void decode(ByteBuf buf) {
		pk = RSAUtils.genPublicKey(Utils.getByteArray(buf));
		GUID = buf.readInt();
		verifyKey = Utils.getString(buf);
	}

	@Override
	public void encode(ByteBuf buf) {
		Utils.writeByteArray(buf, pk.getEncoded());
		buf.writeInt(GUID);
		Utils.writeString(buf, verifyKey);
	}

	@Override
	public void process(PacketMatchCbListener listener) {
		listener.process(this);
	}
}
