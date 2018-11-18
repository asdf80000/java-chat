package chat.common.handler;

import io.netty.buffer.ByteBuf;

public interface ObjectSerializable {
	public void encode(ByteBuf buf);
	public void decode(ByteBuf buf);
}
