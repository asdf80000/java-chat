package chat.common.handler;

import static chat.common.handler.ProtocolDirection.CLIENTBOUND;
import static chat.common.handler.ProtocolDirection.SERVERBOUND;

import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

import chat.common.packet.Packet;
import chat.common.packet.all.PacketAllCbMessage;
import chat.common.packet.all.PacketAllCbSetState;
import chat.common.packet.all.PacketAllSbDisconnect;
import chat.common.packet.login.PacketLoginCbResult;
import chat.common.packet.login.PacketLoginCbWelcome;
import chat.common.packet.login.PacketLoginSbHandshake;
import chat.common.packet.login.PacketLoginSbRegister;
import chat.common.packet.match.PacketMatchCbMatchFound;
import chat.common.packet.match.PacketMatchSbCancelMatchmake;
import chat.common.packet.play.PacketPlayCbChat;
import chat.common.packet.play.PacketPlayCbGetList;
import chat.common.packet.play.PacketPlayCbGetMatchSummary;
import chat.common.packet.play.PacketPlayCbMatchId;
import chat.common.packet.play.PacketPlayCbSafeMessage;
import chat.common.packet.play.PacketPlayCbStart;
import chat.common.packet.play.PacketPlaySbChat;
import chat.common.packet.play.PacketPlaySbGetList;
import chat.common.packet.play.PacketPlaySbGetMatchSummary;
import chat.common.packet.play.PacketPlaySbMatchInfo;
import chat.common.packet.play.PacketPlaySbQuitMatch;
import chat.common.packet.play.PacketPlaySbStart;
import chat.common.packet.user.PacketUserCbSetUsername;
import chat.common.packet.user.PacketUserCbUntranslatedMessage;
import chat.common.packet.user.PacketUserSbGetUsername;
import chat.common.packet.user.PacketUserSbSetUsername;
import chat.common.packet.user.PacketUserSbStartMatchmake;

/**
 * Packet manager. Must contain all packet. Must not duplicate.
 * 
 * @author User
 *
 */
public enum ChannelState {
	/**
	 * Just connected.
	 * 
	 * @see ChannelState.PLAY
	 * @see ChannelState.MATCH
	 * @see ChannelState.USER
	 * @see ChannelState.LOGIN
	 */
	LOGIN(0) {
		{
			add(CLIENTBOUND, PacketLoginCbWelcome.class); // 0
			add(CLIENTBOUND, PacketLoginCbResult.class);

			add(SERVERBOUND, PacketLoginSbHandshake.class); // 0
			add(SERVERBOUND, PacketLoginSbRegister.class);

			addAllPackets(this);
		}
	},
	/**
	 * Sent some important datas.
	 * 
	 * @see ChannelState.PLAY
	 * @see ChannelState.MATCH
	 * @see ChannelState.USER
	 * @see ChannelState.LOGIN
	 */
	USER(1) {
		{
			add(CLIENTBOUND, PacketUserCbSetUsername.class); // 0
			add(CLIENTBOUND, PacketUserCbUntranslatedMessage.class);

			add(SERVERBOUND, PacketUserSbStartMatchmake.class); // 0
			add(SERVERBOUND, PacketUserSbGetUsername.class); // 1
			add(SERVERBOUND, PacketUserSbSetUsername.class); // 2

			addAllPackets(this);
		}
	},
	/**
	 * Matchmaking...
	 * 
	 * @see ChannelState.PLAY
	 * @see ChannelState.MATCH
	 * @see ChannelState.USER
	 * @see ChannelState.LOGIN
	 */
	MATCH(2) {
		{
			add(CLIENTBOUND, PacketMatchCbMatchFound.class); // 0

			add(SERVERBOUND, PacketMatchSbCancelMatchmake.class); // 0

			addAllPackets(this);
		}
	},
	/**
	 * Chatting...
	 * 
	 * @see ChannelState.PLAY
	 * @see ChannelState.MATCH
	 * @see ChannelState.USER
	 * @see ChannelState.LOGIN
	 */
	PLAY(3) {
		{
			add(CLIENTBOUND, PacketPlayCbStart.class); // 0
			add(CLIENTBOUND, PacketPlayCbChat.class); // 1
			add(CLIENTBOUND, PacketPlayCbSafeMessage.class); // 2
			add(CLIENTBOUND, PacketPlayCbGetMatchSummary.class); // 3
			add(CLIENTBOUND, PacketPlayCbGetList.class); // 4
			add(CLIENTBOUND, PacketPlayCbMatchId.class);

			add(SERVERBOUND, PacketPlaySbStart.class); // 0
			add(SERVERBOUND, PacketPlaySbQuitMatch.class); // 1
			add(SERVERBOUND, PacketPlaySbChat.class); // 2
			add(SERVERBOUND, PacketPlaySbGetList.class); // 3
			add(SERVERBOUND, PacketPlaySbMatchInfo.class); // 4
			add(SERVERBOUND, PacketPlaySbGetMatchSummary.class); // 5

			addAllPackets(this);
		}
	};
	public static void addAllPackets(ChannelState cs) {
		cs.add(CLIENTBOUND, PacketAllCbSetState.class); // 1
		cs.add(CLIENTBOUND, PacketAllCbMessage.class); // 2

		cs.add(SERVERBOUND, PacketAllSbDisconnect.class); // 2
	}

	public Map<ProtocolDirection, BiMap<Integer, Class<? extends Packet<?>>>> m;
	private int st = 0;

	ChannelState(int state) {
		// TODO Auto-generated constructor stub
		m = Maps.newEnumMap(ProtocolDirection.class);
		st = state;
	}

	/**
	 * @param n State ID.
	 * @return ChannelState. Null when invalid.
	 */
	public ChannelState getById(int n) {
		for (ChannelState cs : values()) {
			if (cs.st == n) {
				return cs;
			}
		}
		return null;
	}

	public int getId() {
		return st;
	}

	public void add(ProtocolDirection pd, Class<? extends Packet<?>> packet) {
		BiMap<Integer, Class<? extends Packet<?>>> g = m.get(pd);
		if (g == null) {
			g = HashBiMap.create();
			m.put(pd, g);
		}
		g.put(g.size(), packet);
	}
}
