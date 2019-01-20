package chat.common.listener;

import chat.common.packet.user.PacketUserCbSetUsername;
import chat.common.packet.user.PacketUserCbUntranslatedMessage;

public interface PacketUserCbListener extends PacketListener {
	public void process(PacketUserCbSetUsername packet);

	public void process(PacketUserCbUntranslatedMessage packetUserCbUntranslatedMessage);
}
