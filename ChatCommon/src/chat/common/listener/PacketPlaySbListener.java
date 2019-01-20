package chat.common.listener;

import chat.common.packet.play.PacketPlaySbChat;
import chat.common.packet.play.PacketPlaySbGetList;
import chat.common.packet.play.PacketPlaySbGetMatchSummary;
import chat.common.packet.play.PacketPlaySbMatchInfo;
import chat.common.packet.play.PacketPlaySbQuitMatch;
import chat.common.packet.play.PacketPlaySbStart;

public interface PacketPlaySbListener extends PacketListener {
	public void process(PacketPlaySbStart packet);
	public void process(PacketPlaySbChat packetPlaySbChat);
	public void process(PacketPlaySbGetList packetPlaySbGetList);
	public void process(PacketPlaySbQuitMatch packetPlaySbQuitMatch);
	public void process(PacketPlaySbMatchInfo packetPlaySbMatchInfo);
	public void process(PacketPlaySbGetMatchSummary packetPlaySbGetMatchId);
}
