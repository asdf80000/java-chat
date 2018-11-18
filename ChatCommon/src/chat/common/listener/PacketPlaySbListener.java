package chat.common.listener;

import chat.common.packet.play.PacketPlaySbStart;

public interface PacketPlaySbListener extends PacketListener {
	public void process(PacketPlaySbStart packet);
}
