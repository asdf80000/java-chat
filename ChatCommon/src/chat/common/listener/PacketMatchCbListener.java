package chat.common.listener;

import chat.common.packet.match.PacketMatchCbMatchFound;

public interface PacketMatchCbListener extends PacketListener {
	public void process(PacketMatchCbMatchFound packet);
}
