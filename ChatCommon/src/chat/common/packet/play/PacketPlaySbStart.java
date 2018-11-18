package chat.common.packet.play;

import java.security.PublicKey;

import chat.common.listener.PacketPlaySbListener;
import chat.common.main.Utils;
import chat.common.packet.Packet;
import chat.common.work.RSAUtils;
import io.netty.buffer.ByteBuf;

public class PacketPlaySbStart implements Packet<PacketPlaySbListener> {

	public PublicKey cp;
	public int GUID = 0;
	public String verifyKey = "";
	@Override
	public void decode(ByteBuf buf) {
		cp = RSAUtils.genPublicKey(Utils.getByteArray(buf));
		GUID = buf.readInt();
		verifyKey = Utils.getString(buf);
	}

	@Override
	public void encode(ByteBuf buf) {
		Utils.writeByteArray(buf, cp.getEncoded());
		buf.writeInt(GUID);
		Utils.writeString(buf, verifyKey);
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
