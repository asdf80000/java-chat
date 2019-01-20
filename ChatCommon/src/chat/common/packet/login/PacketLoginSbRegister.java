package chat.common.packet.login;

import chat.common.listener.PacketLoginSbListener;
import chat.common.main.Utils;
import chat.common.packet.Packet;
import io.netty.buffer.ByteBuf;

public class PacketLoginSbRegister implements Packet<PacketLoginSbListener> {

	public String username;
	public String password; // SHA256

	@Override
	public void decode(ByteBuf buf) {
		username = Utils.getString(buf);
		password = Utils.getString(buf);
	}

	@Override
	public void encode(ByteBuf buf) {
		Utils.writeString(buf, username);
		Utils.writeString(buf, password);
	}

	@Override
	public void process(PacketLoginSbListener listener) {
		listener.process(this);
	}

}
