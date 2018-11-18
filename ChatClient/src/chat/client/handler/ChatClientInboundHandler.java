package chat.client.handler;

import java.net.InetSocketAddress;

import chat.client.main.ClientMain;
import chat.common.listener.PacketAllCbListener;
import chat.common.listener.PacketLoginCbListener;
import chat.common.listener.PacketMatchCbListener;
import chat.common.listener.PacketUserCbListener;
import chat.common.packet.Packet;
import chat.common.packet.all.PacketAllCbDisconnect;
import chat.common.packet.all.PacketAllCbSetState;
import chat.common.packet.login.PacketLoginCbWelcome;
import chat.common.packet.login.PacketLoginSbHandshake;
import chat.common.packet.match.PacketMatchCbMatchFound;
import chat.common.packet.user.PacketUserCbAnnounce;
import chat.common.packet.user.PacketUserCbSetUsername;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ChatClientInboundHandler extends SimpleChannelInboundHandler<Packet<?>>
		implements PacketLoginCbListener, PacketUserCbListener, PacketAllCbListener, PacketMatchCbListener {
	Channel ch = null;
	public ChannelHandlerContext ctx = null;

	String user = "#unknown";

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ch = ctx.channel();
		this.ctx = ctx;

		PacketLoginSbHandshake hs = new PacketLoginSbHandshake();
		hs.username = "testnick";
		sendPacket(hs);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void processPacket(Packet msg) {
		msg.process(this);
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
	public void process(PacketUserCbSetUsername packet) {
		user = packet.username;
		System.out.println("Server set username: " + user);
	}

	@Override
	public void process(PacketUserCbAnnounce packet) {
		System.out.println("Announce text received!");
		System.out.println("Message: --------------");
		System.out.println(packet.message);
		System.out.println("-----------------------");
	}

	@Override
	public void process(PacketLoginCbWelcome packet) {
		user = packet.username;
		System.out.println("Server set username: " + user);
		System.out.println("Connected to server.");
	}

	@Override
	public void process(PacketAllCbSetState packet) {
		ClientMain.cs = packet.cs;
	}

	@Override
	public void process(PacketAllCbDisconnect packet) {
		System.out.println("[" + getAddress() + "]: Disconnected. Code: " + packet.cc);
		ctx.close().awaitUninterruptibly().syncUninterruptibly();
	}

	public String getAddress() {
		return ((InetSocketAddress) ch.remoteAddress()).getHostName();
	}

	public void sendPacket(Packet<?> pkt) {
		try {
			ctx.writeAndFlush(pkt).await().sync();
		} catch (InterruptedException e) {
		}
	}

	public void sendBlockedPacket(Packet<?> pkt) {
		try {
			ctx.writeAndFlush(pkt).await().sync();
		} catch (InterruptedException e) {
		}
	}

	@Override
	public void process(PacketMatchCbMatchFound packet) {
		
	}
}
