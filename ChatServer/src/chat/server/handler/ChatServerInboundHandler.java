package chat.server.handler;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

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
import chat.common.packet.login.PacketLoginCbResult;
import chat.common.packet.login.PacketLoginCbWelcome;
import chat.common.packet.login.PacketLoginSbHandshake;
import chat.common.packet.login.PacketLoginSbRegister;
import chat.common.packet.match.PacketMatchSbCancelMatchmake;
import chat.common.packet.user.PacketUserCbSetUsername;
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
		checkUserDb();
		String id = packet.id;
		String pw = packet.pwd;
		if(!Utils.isFilenameValid(id)) {
			System.out.printf("[Login][Fail][%s] %s.\r\n", id, "Id not valid filename");
			PacketLoginCbResult p = new PacketLoginCbResult();
			p.succ = false;
			p.key = "INVALID_ID";
			sendPacket(p);
			return;
		}
		File uf = new File("db/users/"+id+".json");
		if(!uf.exists()) {
			System.out.printf("[Login][Fail][%s] %s.\r\n", id, "Not registered account");
			PacketLoginCbResult p = new PacketLoginCbResult();
			p.succ = false;
			p.key = "INVALIDAUTH";
			sendPacket(p);
			return;
		}
		JsonObject job = null;
		try {
			JsonParser jp = new JsonParser();
			job = jp.parse(FileUtils.readFileToString(uf, "UTF8")).getAsJsonObject();
			if(!job.has("nick")) {
				throw new IOException("Old account, create again");
			}
		} catch (IOException | JsonParseException e) {
			FileUtils.deleteQuietly(uf);
			System.out.printf("[Login][Fail][%s] %s.\r\n", id, "Server account removed by error");
			PacketLoginCbResult p = new PacketLoginCbResult();
			p.succ = false;
			p.key = "REGAGAIN";
			sendPacket(p);
			return;
		}
		if(!job.get("pw").getAsString().equals(pw)) {
			System.out.printf("[Login][Fail][%s] %s.\r\n", id, "Wrong password");
			PacketLoginCbResult p = new PacketLoginCbResult();
			p.succ = false;
			p.key = "INVALIDAUTH";
			sendPacket(p);
			return;
		}
		
		PacketLoginCbResult p1 = new PacketLoginCbResult();
		p1.succ = true;
		p1.key = "LOGGED";
		sendPacket(p1);
		
		String username = job.get("nick").getAsString();
		System.out.println("User logged in. Username: " + username);
		Utils.getChannelAttr(AttributeSaver.username, ch).set(username);
		Utils.getChannelAttr(AttributeSaver.id, ch).set(id);
		// DO NOT CHANGE STATE BEFORE SENDING PACKET!!!!!!!!!!!!! IMPORTANT!!
		PacketLoginCbWelcome p = new PacketLoginCbWelcome();
		p.username = username;
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
	}

	@Override
	public void process(PacketUserSbSetUsername packet) {
		Utils.getChannelAttr(AttributeSaver.username, ch).set(packet.username);
		try {
			File uf = new File("db/users/" + Utils.getChannelAttr(AttributeSaver.id, ch) + ".json");
			JsonObject job = null;
			JsonParser jp = new JsonParser();
			job = jp.parse(FileUtils.readFileToString(uf, "UTF8")).getAsJsonObject();
			job.addProperty("nick", packet.username);
			FileUtils.writeStringToFile(uf, new GsonBuilder().serializeNulls().disableHtmlEscaping().setPrettyPrinting().create().toJson(job), "UTF8", false);
		} catch (Exception e) {
		}
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

	@Override
	public void process(PacketLoginSbRegister packet) {
		checkUserDb();
		String id = packet.username;
		String pw = packet.password;
		
		if(!Utils.isFilenameValid(id)) {
			PacketLoginCbResult p = new PacketLoginCbResult();
			p.succ = false;
			p.key = "INVALID_ID";
			sendPacket(p);
			return;
		}
		File uf = new File("db/users/"+id+".json");
		if(uf.exists()) {
			PacketLoginCbResult p = new PacketLoginCbResult();
			p.succ = false;
			p.key = "INVALIDREG";
			sendPacket(p);
			return;
		}
		
		JsonObject job = new JsonObject();
		job.addProperty("id", id);
		job.addProperty("pw", pw);
		job.addProperty("nick", id);
		Gson g = new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();
		try {
			FileUtils.writeStringToFile(uf, g.toJson(job), "UTF8");
		} catch (IOException e) {
			PacketLoginCbResult p = new PacketLoginCbResult();
			p.succ = true;
			p.key = "SERVERIO";
			sendPacket(p);
		}
		
		PacketLoginCbResult p = new PacketLoginCbResult();
		p.succ = true;
		p.key = "CREATED";
		sendPacket(p);
		return;
	}

	public static void checkUserDb() {
		File f = new File("db/users/");
		if (!f.exists())
			f.mkdirs();
	}
}
