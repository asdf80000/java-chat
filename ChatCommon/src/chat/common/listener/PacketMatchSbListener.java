package chat.common.listener;

import chat.common.packet.match.PacketMatchSbCancelMatchmake;

public interface PacketMatchSbListener extends PacketListener {
	public void process(PacketMatchSbCancelMatchmake packet);
}
