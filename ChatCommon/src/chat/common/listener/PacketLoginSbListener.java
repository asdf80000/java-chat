package chat.common.listener;

import chat.common.packet.login.PacketLoginSbHandshake;

public interface PacketLoginSbListener extends PacketListener {
	public void process(PacketLoginSbHandshake packet);
}
