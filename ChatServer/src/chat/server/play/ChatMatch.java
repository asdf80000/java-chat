package chat.server.play;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import chat.common.handler.ChannelState;
import chat.common.main.Utils;
import chat.common.packet.all.PacketAllCbMessage;
import chat.common.packet.all.PacketAllCbMessage.PacketAllCbMessageType;
import chat.common.packet.all.PacketAllCbSetState;
import chat.common.packet.match.PacketMatchCbMatchFound;
import chat.common.packet.play.PacketPlayCbChat;
import chat.common.packet.play.PacketPlayCbSafeMessage;
import chat.common.packet.play.PacketPlaySbChat;
import chat.common.work.Aes256Utils;
import chat.common.work.RSAUtils;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.ScheduledFuture;

public class ChatMatch {
	public final ArrayList<MatchUserData> users;

	public long startTime = System.currentTimeMillis();

	public ChatMatch() {
		users = new ArrayList<>();
		System.out.println("[Match " + hashCodeStr() + "] Constructed and began.");
		setCleanable(false);
	}

	public String hashCodeStr() {
		return Integer.toHexString(hashCode());
	}

	private static abstract class MatchCheckRunnable implements Runnable {
		public ScheduledFuture<?> f;
	}

	private boolean cleanable = false;

	public void setCleanable(boolean value) {
		cleanable = value;
		System.out.println("[Match " + hashCodeStr() + "] Cleanable set to " + value);

	}
	public boolean isCleanable() {
		return cleanable;
	}

	public boolean running = true;
	public LinkedList<ChatsElement> chats = new LinkedList<>();

	public static class ChatsElement {
		public PacketPlaySbChat packetPlaySbChat;
		public String nick;
		public MatchUserData mud;

		public ChatsElement(String nick, PacketPlaySbChat packetPlaySbChat, MatchUserData mud) {
			this.nick = nick;
			this.packetPlaySbChat = packetPlaySbChat;
			this.mud = mud;
		}
	}

	public void startThread(EventLoopGroup bs) {
		MatchCheckRunnable r = new MatchCheckRunnable() {
			boolean has = false;

			@Override
			public void run() {
//				System.out.println("has: " + has + " cleanable: " + cleanable + " size: " + users.size());
				if (users.size() > 0 && cleanable)
					has = true;
				for (int i = 0; i < users.size(); i++) {
					if (!users.get(i).handle.ch.isActive()) {
						MatchUserData dcd = users.remove(i);
						announce(dcd.userName + "님이 방을 나가셨습니다.");
						i--;
					}
				}
//				System.out.println("[Match " + hashCodeStr() + "] Users: " + users.size());
				if (!has || !cleanable)
					return;
				if (users.size() == 0) {
					f.cancel(false);
					running = false;
					System.out.println("[Match " + hashCodeStr() + "] Stopped because player count is 0");
				}
			}
		};
		r.f = bs.scheduleAtFixedRate(r, (int) (Math.random() * 2000), 2000, TimeUnit.MILLISECONDS);

		MatchCheckRunnable r2 = new MatchCheckRunnable() {

			@Override
			public void run() {
				if (!running) {
					f.cancel(false);
					return;
				}
				while (chats.size() > 0) {
					ChatsElement ce = chats.removeFirst();
					String text = Utils.byteStr(Aes256Utils.decrypt(ce.packetPlaySbChat.encText, ce.mud.aesKey));
					for (MatchUserData mud : users) {
						if (!mud.handle.ch.isActive())
							continue;
						PacketPlayCbChat packet = new PacketPlayCbChat();
						packet.chatData = Aes256Utils.encrypt(Utils.byteStr(text), mud.aesKey);
						packet.userNameData = Aes256Utils.encrypt(Utils.byteStr(ce.nick), mud.aesKey);
						mud.handle.sendPacket(packet);
					}
				}
			}
		};
		r2.f = bs.scheduleAtFixedRate(r2, (int) (Math.random() * 100), 100, TimeUnit.MILLISECONDS);
	}

	public boolean addUser(MatchUserData user) {
		if (!users.contains(user)) {
			users.add(user);
			addUsr(user);
			return true;
		} else {
			return false;
		}
	}

	public void announce(String text) {
		for (MatchUserData mud : users) {
			if (!mud.handle.ch.isActive())
				continue;
			tell(text, mud);
		}
	}
	
	public void tell(String text, MatchUserData mud) {
		PacketPlayCbSafeMessage packet = new PacketPlayCbSafeMessage();
		packet.chatData = Aes256Utils.encrypt(Utils.byteStr(text), mud.aesKey);
		mud.handle.sendPacket(packet);
	}

	public void removeUsr(MatchUserData mud) {
		if (mud == null)
			return;
		users.remove(mud);
		mud.currentMatch = null;
		PacketAllCbSetState p = new PacketAllCbSetState();
		p.cs = ChannelState.USER;
		mud.handle.sendPacket(p);
		mud.handle.setState(ChannelState.USER);
		mud.handle.pl = mud.handle.dpl;
		PacketAllCbMessage packet = new PacketAllCbMessage(PacketAllCbMessageType.EXITED);
		mud.handle.sendPacket(packet);
		announce(mud.userName + "님이 방을 나가셨습니다.");
	}

	private void addUsr(MatchUserData mud) {
		mud.handle.pl = mud;
		PacketMatchCbMatchFound p = new PacketMatchCbMatchFound();
		p.pk = RSAUtils.genKey().getPublic();
		mud.handle.sendPacket(p);
		PacketAllCbSetState p1 = new PacketAllCbSetState();
		p1.cs = ChannelState.PLAY;
		mud.handle.sendPacket(p1);
		mud.handle.setState(ChannelState.PLAY);
	}

	public int countUsers() {
		return users.size();
	}
}