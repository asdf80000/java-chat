package chat.server.play;

import static chat.server.play.ChatMatch.activeMatches;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import chat.common.packet.all.PacketAllCbMessage;
import chat.common.packet.all.PacketAllCbMessage.PacketAllCbMessageType;
import io.netty.channel.EventLoopGroup;

public class Matchmaking {
	public static final LinkedList<MatchUserData> matches;
	static {
		matches = new LinkedList<>();
	}

	public static void startThread(EventLoopGroup bs) {
		Runnable r = new Runnable() {
			ChatMatch mc = null;

			@Override
			public void run() {
				System.out.println("[Match] Processing matchmaking tick.");
				// Check match;
				if (mc == null) {
					mc = new ChatMatch();
					mc.startThread(bs);
				}

				// Check able to add player to match
				boolean newMatch = false;
				newMatch = newMatch || mc == null;
				newMatch = newMatch || mc.countUsers() > 10; // Too many users
				newMatch = newMatch || (System.currentTimeMillis() - mc.startTime) > 60000;
				if (newMatch) {
					mc.setCleanable(true);
					mc = new ChatMatch();
					mc.startThread(bs);
				}
				for (int i = 0; i < matches.size(); i++) {
					MatchUserData mud = matches.get(i);
					if (!mud.handle.ch.isActive()) {
						matches.remove(mud);
						i--;
					}
				}

				// merge matches
				for (int i = 0; i < activeMatches.size(); i++) {
					ChatMatch cur = activeMatches.get(i);
					ChatMatch nxt = mc;
					if (cur.equals(nxt))
						continue;
//					if (!cur.running)
//						continue;
					if (cur.lastJoined + 3000 > System.currentTimeMillis())
						continue;
					while (cur.countUsers() > 0) {
						MatchUserData mud = cur.users.get(0);
						if (!mud.handle.ch.isActive()) {
							continue;
						}
						cur.moveUsr(mud, nxt);
					}
				}

				if (matches.size() < 1) { // 매칭 큐 인원 부족, 남은 사용자들은 매칭하지 않음
					return;
				}
				do {
					MatchUserData mud = matches.get((int) (Math.random() * matches.size()));
					if (!mud.handle.ch.isActive()) {
						matches.remove(mud);
						continue;
					}
					System.out.println("[Match] " + mud.userName + " joined match #" + mc.hashCodeStr());

					mud.handle.sendPacket(new PacketAllCbMessage(PacketAllCbMessageType.MATCHFOUND,
							new String[] { mc.hashCodeStr() }));
					mud.currentMatch = mc;
					matches.remove(mud);
					mc.addUser(mud);

					if (mc.countUsers() >= 10) {
						mc.setCleanable(true);
						mc = new ChatMatch();
						mc.startThread(bs);
					}
				} while (matches.size() > 0);
			}
		};
		bs.scheduleAtFixedRate(r, (int) (Math.random() * 10000), 10000, TimeUnit.MILLISECONDS);
	}
}
