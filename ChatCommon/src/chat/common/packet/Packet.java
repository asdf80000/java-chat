package chat.common.packet;

import chat.common.listener.PacketListener;
import io.netty.buffer.ByteBuf;

public interface Packet<T extends PacketListener> {
	public void decode(ByteBuf buf);
	public void encode(ByteBuf buf);
	public void process(T listener);
}
