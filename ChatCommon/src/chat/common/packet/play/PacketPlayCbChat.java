/**
 * 
 */
package chat.common.packet.play;

import chat.common.listener.PacketPlayCbListener;
import chat.common.main.Utils;
import chat.common.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * @author User
 *
 */
public class PacketPlayCbChat implements Packet<PacketPlayCbListener> {

	public byte[] userNameData;
	public byte[] chatData;
	/* (non-Javadoc)
	 * @see chat.common.packet.Packet#decode(io.netty.buffer.ByteBuf)
	 */
	@Override
	public void decode(ByteBuf buf) {
		userNameData = Utils.getByteArray(buf);
		chatData = Utils.getByteArray(buf);
	}

	/* (non-Javadoc)
	 * @see chat.common.packet.Packet#encode(io.netty.buffer.ByteBuf)
	 */
	@Override
	public void encode(ByteBuf buf) {
		Utils.writeByteArray(buf, userNameData);
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
