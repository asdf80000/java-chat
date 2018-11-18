package chat.common.listener;

import chat.common.packet.all.PacketAllSbDisconnect;

public interface PacketAllSbListener extends PacketListener {
	public void process(PacketAllSbDisconnect packet);
}
