package chat.server.play;

import java.security.KeyPair;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import chat.common.listener.PacketAllSbListener;
import chat.common.listener.PacketPlaySbListener;
import chat.common.packet.all.PacketAllSbDisconnect;
import chat.common.packet.play.PacketPlayCbStart;
import chat.common.packet.play.PacketPlaySbChat;
import chat.common.packet.play.PacketPlaySbGetList;
import chat.common.packet.play.PacketPlaySbMatchInfo;
import chat.common.packet.play.PacketPlaySbQuitMatch;
import chat.common.packet.play.PacketPlaySbStart;
import chat.common.work.Aes256Utils;
import chat.server.handler.ChatServerInboundHandler;
import chat.server.play.ChatMatch.ChatsElement;

public class MatchUserData implements PacketPlaySbListener, PacketAllSbListener {
	public final String userAddress;
	public final String userName;
	public final ChatServerInboundHandler handle;
	public KeyPair serverRsaPair;
	public PublicKey clientPublicKey;
	public SecretKey aesKey;
	public ChatMatch currentMatch;

	public MatchUserData(String addr, String name, ChatServerInboundHandler handle) {
		userAddress = addr;
		userName = name;
		this.handle = handle;
	}

	@Override
	public void process(PacketPlaySbStart packet) {
		clientPublicKey = packet.cp;
		aesKey = Aes256Utils.genKey();

		PacketPlayCbStart p = new PacketPlayCbStart();
		p.workwith(clientPublicKey);
		p.sk = aesKey;
		handle.sendPacket(p);

		currentMatch.announce(userName + "님이 방에 참가했습니다.");
		if (currentMatch.countUsers() == 1 && !currentMatch.isCleanable()) {
			currentMatch.tell("아직 이 방으로 매치메이킹이 진행 중입니다.", this);
			currentMatch.tell("방을 옮기지 말고 이 방에서 계속 머무르는 것이 가장 빠르게 사용자를 모으는 방법입니다.", this);
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
		a("이 방의 사용자 목록 (" + cm.countUsers() + "명)");
		for (MatchUserData mud : cm.users) {
			a(" - " + mud.userName + " (" + mud.userAddress.substring(0, Math.min(8, mud.userAddress.length())) + ")");
		}
	}

	private void a(String text) {
		currentMatch.tell(text, this);
	}

	@Override
	public void process(PacketPlaySbQuitMatch packetPlaySbQuitMatch) {
		currentMatch.removeUsr(this);
	}

	@Override
	public void process(PacketPlaySbMatchInfo packetPlaySbMatchInfo) {
		ChatMatch cm = currentMatch;
		a("----- 매치 정보 ----------");
		a("현재 매치 ID: " + cm.hashCodeStr());
		a("현재 매치 인원 수: " + cm.countUsers());
		int mins = 0;
		int secs = (int) ((System.currentTimeMillis() - cm.startTime) / 1000);
		String btxt = "";
		String txt = secs + "초";
		if (secs >= 60) {
			mins = secs / 60;
			secs = secs % 60;
			btxt = " " + secs + "초";
			txt = mins + "분" + btxt;
			
			int hours = 0;
			if (mins >= 60) {
				hours = mins / 60;
				mins = mins % 60;
				btxt = " " + txt;
				txt = hours + "시간" + btxt;
				
				int days = 0;
				if(hours >= 24) {
					days = hours / 24;
					hours = hours % 24;
					btxt = " " + txt;
					txt = days + "일 " + btxt;
					
					int years = 0;
					if(days > 365) {
						years = days / 365;
						days = days % 365;
						btxt = " " + txt;
						txt = years + "년 " + btxt;
					}
				}
			}
		}

		a("현재 매치 진행 시간: " + txt);
	}
}
