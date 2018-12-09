package chat.common.listener;

import chat.common.packet.all.PacketAllCbDisconnect;
import chat.common.packet.all.PacketAllCbMessage;
import chat.common.packet.all.PacketAllCbSetState;

public interface PacketAllCbListener extends PacketListener {
	public void process(PacketAllCbDisconnect packet);
	public void process(PacketAllCbSetState packet);
	public void process(PacketAllCbMessage packetAllCbMessage);
}
