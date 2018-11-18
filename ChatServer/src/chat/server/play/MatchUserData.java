package chat.server.play;

import java.security.KeyPair;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import chat.common.listener.PacketPlaySbListener;
import chat.common.packet.play.PacketPlaySbStart;
import chat.server.handler.ChatServerInboundHandler;

public class MatchUserData implements PacketPlaySbListener {
	public final String address;
	public final String username;
	public final ChatServerInboundHandler handle;
	public KeyPair RsaSec;
	public PublicKey clientKey;
	public int GUID;
	public SecretKey aesKey;
	public ChatMatch curmatch;
	public MatchUserData(String addr, String name, ChatServerInboundHandler handle) {
		address = addr;
		username = name;
		this.handle = handle;
	}
	@Override
	public void process(PacketPlaySbStart packet) {
		
	}
}
