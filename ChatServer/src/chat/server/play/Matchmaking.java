package chat.server.play;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

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
				if(mc == null) {
					mc = new ChatMatch();
				}
				if(mc.countUsers() >= 15) {
					mc = new ChatMatch();
				}
				if(matches.size() < 2) return;
				while(matches.size() > 0 && mc.countUsers() < 15) {
					MatchUserData mud = matches.get((int)(Math.random() * matches.size()));
					System.out.println("Matched: " + mud.userName);
					mud.currentMatch = mc;
					mc.addUser(mud);
				}
			}
		};
		bs.scheduleAtFixedRate(r, 0, 4000, TimeUnit.MILLISECONDS);
	}
}
