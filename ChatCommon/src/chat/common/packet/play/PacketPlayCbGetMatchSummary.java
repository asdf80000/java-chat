package chat.common.packet.play;

import chat.common.listener.PacketPlayCbListener;
import chat.common.main.Utils;
import chat.common.packet.Packet;
import io.netty.buffer.ByteBuf;

public class PacketPlayCbGetMatchSummary implements Packet<PacketPlayCbListener> {
	public String mid = null;

	@Override
	public void decode(ByteBuf buf) {
		mid = Utils.getString(buf);
	}

	@Override
	public void encode(ByteBuf buf) {
		Utils.writeString(buf, mid);
	}

	@Override
	public void process(PacketPlayCbListener listener) {
		listener.process(this);
	}

}
