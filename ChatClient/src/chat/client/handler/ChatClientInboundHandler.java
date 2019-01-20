package chat.client.handler;

import java.net.InetSocketAddress;
import java.security.KeyPair;

import javax.crypto.SecretKey;

import chat.client.main.ClientMain;
import chat.client.work.PacketAllCbMessageToString;
import chat.common.handler.ChannelState;
import chat.common.listener.PacketAllCbListener;
import chat.common.listener.PacketLoginCbListener;
import chat.common.listener.PacketMatchCbListener;
import chat.common.listener.PacketPlayCbListener;
import chat.common.listener.PacketUserCbListener;
import chat.common.main.HandleQueueGeneric;
import chat.common.main.Utils;
import chat.common.packet.Packet;
import chat.common.packet.all.PacketAllCbDisconnect;
import chat.common.packet.all.PacketAllCbMessage;
import chat.common.packet.all.PacketAllCbSetState;
import chat.common.packet.login.PacketLoginCbWelcome;
import chat.common.packet.login.PacketLoginSbHandshake;
import chat.common.packet.match.PacketMatchCbMatchFound;
import chat.common.packet.play.PacketPlayCbChat;
import chat.common.packet.play.PacketPlayCbGetList;
import chat.common.packet.play.PacketPlayCbGetMatchSummary;
import chat.common.packet.play.PacketPlayCbSafeMessage;
import chat.common.packet.play.PacketPlayCbStart;
import chat.common.packet.play.PacketPlaySbChat;
import chat.common.packet.play.PacketPlaySbStart;
import chat.common.packet.user.PacketUserCbSetUsername;
import chat.common.work.Aes256Utils;
import chat.common.work.RSAUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ChatClientInboundHandler extends SimpleChannelInboundHandler<Packet<?>>
		implements PacketLoginCbListener, PacketUserCbListener, PacketAllCbListener, PacketMatchCbListener, PacketPlayCbListener {
	Channel ch = null;
	public ChannelHandlerContext ctx = null;

	String user = "#unknown";

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ch = ctx.channel();
		this.ctx = ctx;

		PacketLoginSbHandshake hs = new PacketLoginSbHandshake();
		//hs.username = "testnick";
		hs.username = "#unknown";
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

	KeyPair client = null;
	@Override
	public void process(PacketMatchCbMatchFound packet) {
		
		ClientMain.pipeline.addQueue(PacketAllCbSetState.class, new HandleQueueGeneric<PacketAllCbSetState>() {

			@Override
			public void run(PacketAllCbSetState p) {
				final HandleQueueGeneric<PacketAllCbSetState> ts = this;
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						if(p.cs != ChannelState.PLAY) {
							ClientMain.pipeline.addQueue(PacketAllCbSetState.class, ts);
							return;
						}
						while(ClientMain.cs != ChannelState.PLAY) {
							Utils.sleep();
						}
						client = RSAUtils.genKey();
						PacketPlaySbStart packet = new PacketPlaySbStart();
						packet.cp = client.getPublic();
						PacketPlayCbStart.decodeworkwith(client.getPrivate());
						sendPacket(packet);
					}
				}).start();
			}
		});
	}
	
	public SecretKey aeskey;

	@Override
	public void process(PacketPlayCbStart packet) {
		aeskey = packet.sk;
	}
	
	public void chat(String text) {
		if(ClientMain.cs != ChannelState.PLAY) return;
		PacketPlaySbChat packet = new PacketPlaySbChat();
		packet.encText = Aes256Utils.encrypt(Utils.byteStr(text), aeskey);
		sendPacket(packet);
	}

	@Override
	public void process(PacketPlayCbChat packetPlayCbChat) {
		String nick = Utils.byteStr(Aes256Utils.decrypt(packetPlayCbChat.userNameData, aeskey));
		String text = Utils.byteStr(Aes256Utils.decrypt(packetPlayCbChat.chatData, aeskey));
		System.out.println("[" + nick + "] " + text);
	}

	@Override
	public void process(PacketPlayCbSafeMessage packet) {
		String text = Utils.byteStr(Aes256Utils.decrypt(packet.chatData, aeskey));
		System.out.println("<서버 메시지> " + text);
	}

	@Override
	public void process(PacketAllCbMessage packetAllCbMessage) {
		System.out.println("<서버 메시지> " + PacketAllCbMessageToString.getString(packetAllCbMessage.type, packetAllCbMessage.array));
	}

	@Override
	public void process(PacketPlayCbGetMatchSummary packetPlayCbGetMatchId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void process(PacketPlayCbGetList packetPlayCbGetList) {
		// TODO Auto-generated method stub
		
	}
}
