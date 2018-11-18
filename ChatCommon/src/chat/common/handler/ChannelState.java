package chat.common.handler;

import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

import chat.common.packet.Packet;
import chat.common.packet.all.PacketAllCbSetState;
import chat.common.packet.all.PacketAllSbDisconnect;
import chat.common.packet.login.PacketLoginCbWelcome;
import chat.common.packet.login.PacketLoginSbHandshake;
import chat.common.packet.match.PacketMatchCbMatchFound;
import chat.common.packet.match.PacketMatchSbCancelMatchmake;
import chat.common.packet.play.PacketPlayCbStart;
import chat.common.packet.play.PacketPlaySbStart;
import chat.common.packet.user.PacketUserCbAnnounce;
import chat.common.packet.user.PacketUserCbSetUsername;
import chat.common.packet.user.PacketUserSbGetUsername;
import chat.common.packet.user.PacketUserSbSetUsername;
import chat.common.packet.user.PacketUserSbStartMatchmake;

public enum ChannelState {
	LOGIN(0) {
		{
			add(ProtocolDirection.CLIENTBOUND, PacketLoginCbWelcome.class);
			add(ProtocolDirection.CLIENTBOUND, PacketAllCbSetState.class);

			add(ProtocolDirection.SERVERBOUND, PacketLoginSbHandshake.class);
			add(ProtocolDirection.SERVERBOUND, PacketAllSbDisconnect.class);
		}
	},
	USER(1) {
		{
			add(ProtocolDirection.CLIENTBOUND, PacketUserCbSetUsername.class);
			add(ProtocolDirection.CLIENTBOUND, PacketUserCbAnnounce.class);
			add(ProtocolDirection.CLIENTBOUND, PacketAllCbSetState.class);

			add(ProtocolDirection.SERVERBOUND, PacketUserSbStartMatchmake.class);
			add(ProtocolDirection.SERVERBOUND, PacketUserSbGetUsername.class);
			add(ProtocolDirection.SERVERBOUND, PacketUserSbSetUsername.class);
			add(ProtocolDirection.SERVERBOUND, PacketAllSbDisconnect.class);
		}
	},
	MATCH(2) {
		{
			add(ProtocolDirection.CLIENTBOUND, PacketMatchCbMatchFound.class);
			add(ProtocolDirection.CLIENTBOUND, PacketAllCbSetState.class);

			add(ProtocolDirection.SERVERBOUND, PacketMatchSbCancelMatchmake.class);
			add(ProtocolDirection.SERVERBOUND, PacketAllSbDisconnect.class);
		}
	},
	PLAY(3) {
		{
			add(ProtocolDirection.CLIENTBOUND, PacketPlayCbStart.class);
			add(ProtocolDirection.CLIENTBOUND, PacketAllCbSetState.class);
			
			add(ProtocolDirection.SERVERBOUND, PacketPlaySbStart.class);
			add(ProtocolDirection.SERVERBOUND, PacketAllSbDisconnect.class);
		}
	};
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
