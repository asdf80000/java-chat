package chat.common.listener;

import chat.common.packet.login.PacketLoginCbWelcome;

public interface PacketLoginCbListener extends PacketListener {
	public void process(PacketLoginCbWelcome packet);
}
