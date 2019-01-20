package chat.server.play;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;

import chat.common.listener.PacketAllSbListener;
import chat.common.listener.PacketPlaySbListener;
import chat.common.main.Utils;
import chat.common.packet.all.PacketAllSbDisconnect;
import chat.common.packet.play.PacketPlayCbGetList;
import chat.common.packet.play.PacketPlayCbGetMatchSummary;
import chat.common.packet.play.PacketPlayCbStart;
import chat.common.packet.play.PacketPlaySbChat;
import chat.common.packet.play.PacketPlaySbGetList;
import chat.common.packet.play.PacketPlaySbGetMatchSummary;
import chat.common.packet.play.PacketPlaySbMatchInfo;
import chat.common.packet.play.PacketPlaySbQuitMatch;
import chat.common.packet.play.PacketPlaySbStart;
import chat.common.packet.play.PacketPlayCbSafeMessage.PacketPlayCbSafeMessageType;
import chat.common.work.Aes256Utils;
import chat.server.handler.ChatServerInboundHandler;
import chat.server.play.ChatMatch.ChatsElement;

public class MatchUserData implements PacketPlaySbListener, PacketAllSbListener {
	public final String userAddress;
	public final String userName;
	public final ChatServerInboundHandler handle;
	public KeyPair serverRsaPair;
	public PublicKey clientPublicKey;
	public ChatMatch currentMatch;
	public boolean ready = false;

	public MatchUserData(String addr, String name, ChatServerInboundHandler handle) {
		userAddress = addr;
		userName = name;
		this.handle = handle;
	}

	@Override
	public void process(PacketPlaySbStart packet) {
		clientPublicKey = packet.cp;

		PacketPlayCbStart p = new PacketPlayCbStart();
		p.workwith(clientPublicKey);
		p.sk = currentMatch.aeskey;
		handle.sendPacket(p);
		
		ready = true;

		currentMatch.announce(PacketPlayCbSafeMessageType.JOINED, new String[] { userName });
		if (currentMatch.countUsers() == 1 && !currentMatch.isCleanable()) {
			currentMatch.tell(PacketPlayCbSafeMessageType.STILL_0, null, this);
			currentMatch.tell(PacketPlayCbSafeMessageType.STILL_1, null, this);
			currentMatch.tell(PacketPlayCbSafeMessageType.STILL_2, null, this);
		}
	}

	@Override
	public void process(PacketAllSbDisconnect packet) {
		handle.process(packet);
	}

	@Override
	public void process(PacketPlaySbChat packetPlaySbChat) {
		currentMatch.chats.add(new ChatsElement(userName, packetPlaySbChat, this));
	}

	@Override
	public void process(PacketPlaySbGetList packetPlaySbGetList) {
		ChatMatch cm = currentMatch;
		PacketPlayCbGetList p = new PacketPlayCbGetList();
		p.list = new ArrayList<>();
		for (MatchUserData mud : cm.users) {
			String ts = mud.userName + "(" + mud.userAddress.substring(0, 4) + ")";
			p.list.add(Aes256Utils.encrypt(Utils.byteStr(ts), cm.aeskey));
		}
		handle.sendPacket(p);
	}

	private void tell(PacketPlayCbSafeMessageType type, String[] dataArr) {
		currentMatch.tell(type, dataArr, this);
	}

	@Override
	public void process(PacketPlaySbQuitMatch packetPlaySbQuitMatch) {
		currentMatch.removeUsr(this);
	}

	@Override
	public void process(PacketPlaySbMatchInfo packetPlaySbMatchInfo) {
		ChatMatch cm = currentMatch;
		tell(PacketPlayCbSafeMessageType.MATCHINFO_0, null);
		tell(PacketPlayCbSafeMessageType.MATCHINFO_1, new String[] { cm.hashCodeStr() });
		tell(PacketPlayCbSafeMessageType.MATCHINFO_2, new String[] { cm.countUsers() + "" });
		long l = (System.currentTimeMillis() - cm.startTime);
		tell(PacketPlayCbSafeMessageType.MATCHINFO_3, new String[] { l + "" });
	}

	@Override
	public void process(PacketPlaySbGetMatchSummary packetPlaySbGetMatchSummary) {
		PacketPlayCbGetMatchSummary p = new PacketPlayCbGetMatchSummary();
		p.mid = currentMatch.hashCodeStr();
		handle.sendPacket(p);
	}
}
