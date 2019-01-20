package chat.common.listener;

import chat.common.packet.play.PacketPlayCbSafeMessage;
import chat.common.packet.play.PacketPlayCbChat;
import chat.common.packet.play.PacketPlayCbGetList;
import chat.common.packet.play.PacketPlayCbGetMatchSummary;
import chat.common.packet.play.PacketPlayCbMatchId;
import chat.common.packet.play.PacketPlayCbStart;

public interface PacketPlayCbListener extends PacketListener {
	public void process(PacketPlayCbStart packet);
	public void process(PacketPlayCbChat packetPlayCbChat);
	public void process(PacketPlayCbSafeMessage packetPlayCbAnnounce);
	public void process(PacketPlayCbGetMatchSummary packetPlayCbGetMatchId);
	public void process(PacketPlayCbGetList packetPlayCbGetList);
	public void process(PacketPlayCbMatchId packetPlayCbMatchId);
}
