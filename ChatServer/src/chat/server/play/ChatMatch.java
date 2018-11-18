package chat.server.play;

import java.util.ArrayList;
import java.util.HashMap;

import chat.common.handler.ChannelState;
import chat.common.listener.PacketPlaySbListener;
import chat.common.main.Utils;
import chat.common.packet.all.PacketAllCbSetState;
import chat.common.packet.match.PacketMatchCbMatchFound;
import chat.common.packet.play.PacketPlayCbStart;
import chat.common.packet.play.PacketPlaySbStart;
import chat.common.work.Aes256Utils;
import chat.common.work.RSAUtils;

public class ChatMatch implements PacketPlaySbListener {
	private final ArrayList<MatchUserData> users;
	private final HashMap<Integer, MatchUserData> usermap;
	private final HashMap<Integer, String> veriMap;
	private final HashMap<String, MatchUserData> keyMap;

	public ChatMatch() {
		users = new ArrayList<>();
		usermap = new HashMap<>();
		veriMap = new HashMap<>();
		keyMap = new HashMap<>();
	}

	public boolean addUser(MatchUserData user) {
		if (!users.contains(user)) {
			users.add(user);
			int g = (int) (Math.random() * 2147483647);
			while (usermap.containsKey(g)) {
				g = (int) (Math.random() * 2147483647);
			}
			addUsr(user, g);
			return true;
		} else {
			return false;
		}
	}

	private void removeUsr(MatchUserData mud) {
		if (mud == null)
			return;
		users.remove(mud);
		usermap.remove(mud.GUID);
		veriMap.remove(mud.GUID);
		keyMap.remove(veriMap.get(mud.GUID));
		mud.curmatch = null;
	}

	private void addUsr(MatchUserData mud, int GUID) {
		mud.handle.pl = mud;
		usermap.put(GUID, mud);
		String key = Utils.randomStr(100);
		veriMap.put(GUID, key);
		mud.handle.pl = this;
		mud.GUID = GUID;
		PacketMatchCbMatchFound p = new PacketMatchCbMatchFound();
		p.pk = RSAUtils.genKey().getPublic();
		p.GUID = GUID;
		p.verifyKey = key;
		mud.handle.sendPacket(p);
		PacketAllCbSetState p1 = new PacketAllCbSetState();
		p1.cs = ChannelState.PLAY;
		mud.handle.sendPacket(p1);
		mud.handle.setState(ChannelState.PLAY);
	}

	public int countUsers() {
		return users.size();
	}

	@Override
	public void process(PacketPlaySbStart packet) {
		if (!veriMap.get(packet.GUID).equals(packet.verifyKey)) {
			removeUsr(keyMap.get(packet.verifyKey));
			return;
		}
		MatchUserData mu = usermap.get(packet.GUID);
		mu.clientKey = packet.cp;
		mu.aesKey = Aes256Utils.genKey();

		PacketPlayCbStart p = new PacketPlayCbStart();
		p.workwith(mu.clientKey);
		p.sk = mu.aesKey;
		mu.handle.sendPacket(p);
	}
}