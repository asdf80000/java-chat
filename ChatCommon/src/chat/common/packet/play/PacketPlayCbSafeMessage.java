/**
 * 
 */
package chat.common.packet.play;

import javax.crypto.SecretKey;

import chat.common.listener.PacketPlayCbListener;
import chat.common.main.Utils;
import chat.common.packet.Packet;
import chat.common.work.Aes256Utils;
import io.netty.buffer.ByteBuf;

/**
 * @author User
 *
 */
public class PacketPlayCbSafeMessage implements Packet<PacketPlayCbListener> {

	public byte[] chatData;
	public void setText(String text, SecretKey sk) {
		chatData = Aes256Utils.encrypt(Utils.byteStr(text), sk);
	}
	/* (non-Javadoc)
	 * @see chat.common.packet.Packet#decode(io.netty.buffer.ByteBuf)
	 */
	@Override
	public void decode(ByteBuf buf) {
		chatData = Utils.getByteArray(buf);
	}

	/* (non-Javadoc)
	 * @see chat.common.packet.Packet#encode(io.netty.buffer.ByteBuf)
	 */
	@Override
	public void encode(ByteBuf buf) {
		Utils.writeByteArray(buf, chatData);
	}

	/* (non-Javadoc)
	 * @see chat.common.packet.Packet#process(chat.common.listener.PacketListener)
	 */
	@Override
	public void process(PacketPlayCbListener listener) {
		listener.process(this);
	}

}
