package chat.common.listener;

import chat.common.packet.login.PacketLoginCbResult;
import chat.common.packet.login.PacketLoginCbWelcome;

public interface PacketLoginCbListener extends PacketListener {
	public void process(PacketLoginCbWelcome packet);

	public void process(PacketLoginCbResult packetLoginCbResult);
}
