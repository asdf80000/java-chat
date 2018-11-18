package chat.common.listener;

import chat.common.packet.user.PacketUserCbAnnounce;
import chat.common.packet.user.PacketUserCbSetUsername;

public interface PacketUserCbListener extends PacketListener {
	public void process(PacketUserCbSetUsername packet);
	public void process(PacketUserCbAnnounce packet);
}
