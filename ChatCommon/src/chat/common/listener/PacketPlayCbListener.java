package chat.common.listener;

import chat.common.packet.play.PacketPlayCbStart;

public interface PacketPlayCbListener extends PacketListener {
	public void process(PacketPlayCbStart packet);
}
