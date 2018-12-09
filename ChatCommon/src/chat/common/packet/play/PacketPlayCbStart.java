package chat.common.packet.play;

import java.security.Key;

import javax.crypto.SecretKey;

import chat.common.listener.PacketPlayCbListener;
import chat.common.main.Utils;
import chat.common.packet.Packet;
import chat.common.work.Aes256Utils;
import chat.common.work.RSAUtils;
import io.netty.buffer.ByteBuf;

/**
 * 
 * @see PacketPlaySbStart
 * @author User
 *
 */
public class PacketPlayCbStart implements Packet<PacketPlayCbListener> {
	private Key ww = null;
	private static Key dww = null;
	public PacketPlayCbStart() {
	}
	public void workwith(Key w) {
		ww = w;
	}
	public static void decodeworkwith(Key w) {
		dww = w;
	}
	public SecretKey sk;
	@Override
	public void decode(ByteBuf buf) {
		sk = Aes256Utils.genKey(RSAUtils.decrypt(Utils.getByteArray(buf), dww));
	}

	@Override
	public void encode(ByteBuf buf) {
		Utils.writeByteArray(buf, RSAUtils.encrypt(sk.getEncoded(), ww));
	}

	@Override
	public void process(PacketPlayCbListener listener) {
		listener.process(this);
	}

}
