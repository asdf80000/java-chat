package chat.common.listener;

import chat.common.packet.login.PacketLoginSbHandshake;
import chat.common.packet.login.PacketLoginSbRegister;

public interface PacketLoginSbListener extends PacketListener {
	public void process(PacketLoginSbHandshake packet);

	public void process(PacketLoginSbRegister packetLoginSbRegister);
}
