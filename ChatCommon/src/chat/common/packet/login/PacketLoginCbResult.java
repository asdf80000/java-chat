package chat.common.packet.login;

import chat.common.listener.PacketLoginCbListener;
import chat.common.main.Utils;
import chat.common.packet.Packet;
import io.netty.buffer.ByteBuf;

public class PacketLoginCbResult implements Packet<PacketLoginCbListener> {
	public String key = "";
	public boolean succ = false;
	@Override
	public void decode(ByteBuf buf) {
		key = Utils.getString(buf);
		succ = buf.readBoolean();
	}

	@Override
	public void encode(ByteBuf buf) {
		Utils.writeString(buf, key);
		buf.writeBoolean(succ);
	}

	@Override
	public void process(PacketLoginCbListener listener) {
		listener.process(this);
	}

}
