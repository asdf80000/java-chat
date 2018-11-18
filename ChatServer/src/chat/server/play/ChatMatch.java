package chat.server.play;

import java.util.ArrayList;

import chat.common.handler.ChannelState;
import chat.common.packet.all.PacketAllCbSetState;
import chat.common.packet.match.PacketMatchCbMatchFound;
import chat.common.work.RSAUtils;

public class ChatMatch {
	private final ArrayList<MatchUserData> users;

	public ChatMatch() {
		users = new ArrayList<>();
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

	private void removeUsr(MatchUserData mud) {
		if (mud == null)
			return;
		users.remove(mud);
		mud.currentMatch = null;
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