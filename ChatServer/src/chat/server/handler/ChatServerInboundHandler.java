package chat.server.handler;

import java.net.InetSocketAddress;

import chat.common.handler.ChannelState;
import chat.common.listener.PacketAllSbListener;
import chat.common.listener.PacketListener;
import chat.common.listener.PacketLoginSbListener;
import chat.common.listener.PacketMatchSbListener;
import chat.common.listener.PacketUserSbListener;
import chat.common.main.Utils;
import chat.common.packet.Packet;
import chat.common.packet.all.PacketAllCbMessage;
import chat.common.packet.all.PacketAllCbMessage.PacketAllCbMessageType;
import chat.common.packet.all.PacketAllCbSetState;
import chat.common.packet.all.PacketAllSbDisconnect;
import chat.common.packet.login.PacketLoginCbWelcome;
import chat.common.packet.login.PacketLoginSbHandshake;
import chat.common.packet.match.PacketMatchSbCancelMatchmake;
import chat.common.packet.user.PacketUserCbSetUsername;
import chat.common.packet.user.PacketUserCbUntranslatedMessage;
import chat.common.packet.user.PacketUserSbGetUsername;
import chat.common.packet.user.PacketUserSbSetUsername;
import chat.common.packet.user.PacketUserSbStartMatchmake;
import chat.server.play.MatchUserData;
import chat.server.play.Matchmaking;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ChatServerInboundHandler extends SimpleChannelInboundHandler<Packet<?>>
		implements PacketLoginSbListener, PacketUserSbListener, PacketAllSbListener, PacketMatchSbListener {
	public Channel ch = null;
	public ChannelHandlerContext ctx = null;
	public PacketListener pl = null;
	public PacketListener dpl = null;
	public int ChatGUID = 0;

	public void setState(ChannelState newState) {
		Utils.getChannelAttr(AttributeSaver.state, ctx.channel()).set(newState);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		ch = ctx.channel();
		this.ctx = ctx;
		Utils.getChannelAttr(AttributeSaver.state, ctx.channel()).set(ChannelState.LOGIN);
		pl = this;
		dpl = this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void processPacket(Packet msg) {
		msg.process(pl);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Packet<?> msg) throws Exception {
		processPacket(msg);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		ctx.flush();
	}

	@Override
	public void process(PacketUserSbGetUsername packet) {
		PacketUserCbSetUsername p = new PacketUserCbSetUsername();
		String username = Utils.getChannelAttr(AttributeSaver.username, ch).get();
		p.username = username;
		sendPacket(p);

		if (packet.message) {
			PacketAllCbMessage p2 = new PacketAllCbMessage(PacketAllCbMessageType.YOURNICK, new String[] { username });
			sendPacket(p2);
		}
	}

	@Override
	public void process(PacketLoginSbHandshake packet) {
		System.out.println("User logged in. Username: " + packet.username);
		Utils.getChannelAttr(AttributeSaver.username, ch).set(packet.username);
		// DO NOT CHANGE STATE BEFORE SENDING PACKET!!!!!!!!!!!!! IMPORTANT!!
		PacketLoginCbWelcome p = new PacketLoginCbWelcome();
		p.username = packet.username;
		sendPacket(p);
		ctx.flush();

		;
		{
			PacketAllCbSetState st = new PacketAllCbSetState();
			st.cs = ChannelState.USER;
			sendPacket(st);
		}
		Utils.getChannelAttr(AttributeSaver.state, ch).set(ChannelState.USER);
		sendPacket(new PacketAllCbMessage(PacketAllCbMessageType.HELLO));

		{
			PacketUserCbUntranslatedMessage pkt = new PacketUserCbUntranslatedMessage(
					"Please note that our server never log online users' chatting. And currently we never collect user informations.");
			sendPacket(pkt);
		}
		{
			PacketUserCbUntranslatedMessage pkt = new PacketUserCbUntranslatedMessage(
					"저희 서버는 사용자의 채팅과 개인 정보를 수집/기록하지 않는다는 사실을 알아두십시오.");
			sendPacket(pkt);
		}
	}

	@Override
	public void process(PacketUserSbSetUsername packet) {
		Utils.getChannelAttr(AttributeSaver.username, ch).set(packet.username);
		sendPacket(new PacketAllCbMessage(PacketAllCbMessageType.NICKCHANGED));
	}

	@Override
	public void process(PacketUserSbStartMatchmake packet) {
		{
			PacketAllCbSetState p = new PacketAllCbSetState();
			p.cs = ChannelState.MATCH;
			sendPacket(p);
		}
		Utils.getChannelAttr(AttributeSaver.state, ch).set(ChannelState.MATCH);
		System.out.println("[Handle][" + getUsername() + "] Started matchmaking.");
		mud = new MatchUserData(getAddress(), Utils.getChannelAttr(AttributeSaver.username, ch).get(), this);
		Matchmaking.matches.add(mud);
		sendPacket(new PacketAllCbMessage(PacketAllCbMessageType.MATCHSTARTED));
	}

	MatchUserData mud = null;

	@Override
	public void process(PacketMatchSbCancelMatchmake packet) {
		{
			PacketAllCbSetState p = new PacketAllCbSetState();
			p.cs = ChannelState.USER;
			sendPacket(p);
		}
		Utils.getChannelAttr(AttributeSaver.state, ch).set(ChannelState.USER);
		System.out.println("[Handle][" + getUsername() + "] Canceled matchmaking.");
		Matchmaking.matches.remove(mud);
		mud = null;
	}

	public String getAddress() {
		return ((InetSocketAddress) ch.remoteAddress()).getHostString();
	}

	public String getUsername() {
		return Utils.getChannelAttr(AttributeSaver.username, ch).get();
	}

	@Override
	public void process(PacketAllSbDisconnect packet) {
		System.out.println("[" + getAddress() + "]: Disconnected. Code: " + packet.cc);
		ctx.close().awaitUninterruptibly().syncUninterruptibly();
	}

	public void sendPacket(Packet<?> pkt) {
		try {
			ctx.writeAndFlush(pkt).awaitUninterruptibly().sync();
		} catch (InterruptedException e) {
		}
	}
}
