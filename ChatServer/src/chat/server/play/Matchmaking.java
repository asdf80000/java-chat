package chat.server.play;

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
//			boolean flushing = false;

			@Override
			public void run() {
				if (matches.size() < 1)
					return;
				do {
					// Check match
					if (mc == null) {
						mc = new ChatMatch();
						mc.startThread(bs);
					}
					// Check able to add player to match
					boolean newMatch = false;
					newMatch = newMatch || mc.countUsers() > 50; // Too many users
					newMatch = newMatch || System.currentTimeMillis() - mc.startTime > 60000; // Too old(60sec) match
					if (newMatch) {
						mc.setCleanable(true);
						mc.announce("이제부터 이 방에는 새로운 사용자가 들어오지 않을 것입니다. 만약 혼자라면 다른 방을 찾는 것을 권장합니다.");
						mc = new ChatMatch();
						mc.startThread(bs);
					}

					if (matches.size() < 1)
						continue;
					MatchUserData mud = matches.get((int) (Math.random() * matches.size()));
					if (!mud.handle.ch.isActive()) {
						matches.remove(mud);
						continue;
					}
					System.out.println("Matched: " + mud.userName);

					mud.handle.sendPacket(new PacketAllCbMessage(PacketAllCbMessageType.MATCHFOUND,
							new String[] { mc.hashCodeStr() }));
					mud.currentMatch = mc;
					mc.addUser(mud);
					matches.remove(mud);
				} while (matches.size() > 0);
			}
		};
		bs.scheduleAtFixedRate(r, (int) (Math.random() * 5000), 5000, TimeUnit.MILLISECONDS);
	}
}
