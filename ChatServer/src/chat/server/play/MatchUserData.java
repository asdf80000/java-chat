package chat.server.play;

import java.security.KeyPair;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import chat.common.listener.PacketPlaySbListener;
import chat.common.packet.play.PacketPlayCbStart;
import chat.common.packet.play.PacketPlaySbStart;
import chat.common.work.Aes256Utils;
import chat.server.handler.ChatServerInboundHandler;

public class MatchUserData implements PacketPlaySbListener {
	public final String userAddress;
	public final String userName;
	public final ChatServerInboundHandler handle;
	public KeyPair serverRsaPair;
	public PublicKey clientPublicKey;
	public SecretKey aesKey;
	public ChatMatch currentMatch;
	public MatchUserData(String addr, String name, ChatServerInboundHandler handle) {
		userAddress = addr;
		userName = name;
		this.handle = handle;
	}
	@Override
	public void process(PacketPlaySbStart packet) {
		clientPublicKey = packet.cp;
		aesKey = Aes256Utils.genKey();

		PacketPlayCbStart p = new PacketPlayCbStart();
		p.workwith(clientPublicKey);
		p.sk = aesKey;
		handle.sendPacket(p);
	}
}
