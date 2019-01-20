package chat.server.play;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import javax.crypto.SecretKey;

import chat.common.handler.ChannelState;
import chat.common.main.Utils;
import chat.common.packet.all.PacketAllCbMessage;
import chat.common.packet.all.PacketAllCbMessage.PacketAllCbMessageType;
import chat.common.packet.all.PacketAllCbSetState;
import chat.common.packet.match.PacketMatchCbMatchFound;
import chat.common.packet.play.PacketPlayCbChat;
import chat.common.packet.play.PacketPlayCbMatchId;
import chat.common.packet.play.PacketPlayCbSafeMessage;
import chat.common.packet.play.PacketPlayCbSafeMessage.PacketPlayCbSafeMessageType;
import chat.common.packet.play.PacketPlayCbStart;
import chat.common.packet.play.PacketPlaySbChat;
import chat.common.work.Aes256Utils;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.ScheduledFuture;

public class ChatMatch {
	public final ArrayList<MatchUserData> users;
	public static final ArrayList<ChatMatch> activeMatches;
	static {
		activeMatches = new ArrayList<>();
	}

	public long startTime = System.currentTimeMillis();
	public long lastJoined = 0;

	public ChatMatch() {
		users = new ArrayList<>();
		System.out.println("[Match " + hashCodeStr() + "] Constructed and began.");
		setCleanable(false);
		aeskey = Aes256Utils.genKey();
		activeMatches.add(this);
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
				if (cleanable)
					has = true;
				for (int i = 0; i < users.size(); i++) {
					if (!users.get(i).handle.ch.isActive()) {
						MatchUserData dcd = users.remove(i);
						announce(PacketPlayCbSafeMessageType.EXITED, new String[] { dcd.userName });
						i--;
					}
				}
//				System.out.println("[Match " + hashCodeStr() + "] Users: " + users.size());
				if (!has || !cleanable) {
					return;
				}
				if (users.size() == 0) {
					f.cancel(false);
					running = false;
					activeMatches.remove(ChatMatch.this);
					System.out.println("[Match " + hashCodeStr() + "] Cleaned empty room");
				}
			}
		};
		r.f = bs.scheduleAtFixedRate(r, (int) (Math.random() * 2000), 2000, TimeUnit.MILLISECONDS);

		MatchCheckRunnable r2 = new MatchCheckRunnable() {

			public static final int MAX_CHAT_LENGTH = 100;
			public static final int MIN_CHAT_LENGTH = 1;

			@Override
			public void run() {
				if (!running) {
					f.cancel(false);
					return;
				}
				while (chats.size() > 0) {
					ChatsElement ce = chats.removeFirst();
					byte[] bs = Aes256Utils.decrypt(ce.packetPlaySbChat.encText, aeskey);
					if (bs.length > MAX_CHAT_LENGTH) {
						tell(PacketPlayCbSafeMessageType.MSG_TOO_LONG, new String[] { MAX_CHAT_LENGTH + "" }, ce.mud);
						continue;
					}
					if (bs.length < MIN_CHAT_LENGTH) {
						tell(PacketPlayCbSafeMessageType.MSG_TOO_SHORT, new String[] { MIN_CHAT_LENGTH + "" }, ce.mud);
						continue;
					}
					String text = Utils.byteStr(bs);
					bs = null;
					for (MatchUserData mud : users) {
						if (!mud.handle.ch.isActive())
							continue;
						PacketPlayCbChat packet = new PacketPlayCbChat();
						packet.chatData = Aes256Utils.encrypt(Utils.byteStr(text), aeskey);
						packet.userNameData = Aes256Utils.encrypt(Utils.byteStr(ce.nick), aeskey);
						mud.handle.sendPacket(packet);
					}
				}
				if (users.size() < 10 && !isCleanable()) {

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

	public void announce(PacketPlayCbSafeMessageType type, String[] dataArr) {
		for (MatchUserData mud : users) {
			if (!mud.handle.ch.isActive())
				continue;
			tell(type, dataArr, mud);
		}
	}

	public void tell(PacketPlayCbSafeMessageType type, String[] dataArr, MatchUserData mud) {
		PacketPlayCbSafeMessage packet = new PacketPlayCbSafeMessage();
		packet.setAesWritingKey(aeskey);
		packet.type = type;
		packet.array = dataArr;
		if (mud.ready) {
			mud.handle.sendPacket(packet);
		} else {
			new Thread(new Runnable() {

				@Override
				public void run() {
					while (!mud.ready) {
						Utils.sleep();
					}
					mud.handle.sendPacket(packet);
				}
			}).start();
		}
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
		announce(PacketPlayCbSafeMessageType.EXITED, new String[] { mud.userName });
	}

	/**
	 * Change state to match
	 * 
	 * @param mud
	 */
	public void moveUsr(MatchUserData mud, ChatMatch newMatch) {
		if (mud == null)
			return;
		users.remove(mud);
		mud.currentMatch = newMatch;
		newMatch.users.add(mud);

		PacketPlayCbStart p = new PacketPlayCbStart();
		p.workwith(mud.clientPublicKey);
		p.sk = newMatch.aeskey;
		mud.handle.sendPacket(p);

		newMatch.lastJoined = System.currentTimeMillis();

		PacketPlayCbMatchId p1 = new PacketPlayCbMatchId();
		p1.newId = newMatch.hashCodeStr();
		mud.handle.sendPacket(p1);
	}

	private void addUsr(MatchUserData mud) {
		mud.handle.pl = mud;
		PacketMatchCbMatchFound p = new PacketMatchCbMatchFound();
		mud.handle.sendPacket(p);
		PacketAllCbSetState p1 = new PacketAllCbSetState();
		p1.cs = ChannelState.PLAY;
		mud.handle.sendPacket(p1);
		mud.handle.setState(ChannelState.PLAY);
		lastJoined = System.currentTimeMillis();
	}

	public SecretKey aeskey = null;

	public int countUsers() {
		return users.size();
	}
}