package chat.common.listener;

import chat.common.packet.user.PacketUserSbGetUsername;
import chat.common.packet.user.PacketUserSbSetUsername;
import chat.common.packet.user.PacketUserSbStartMatchmake;

public interface PacketUserSbListener extends PacketListener {
	public void process(PacketUserSbGetUsername packet);
	public void process(PacketUserSbSetUsername packet);
	public void process(PacketUserSbStartMatchmake packet);
}
