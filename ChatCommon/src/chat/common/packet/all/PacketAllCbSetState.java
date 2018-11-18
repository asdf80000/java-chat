package chat.common.packet.all;

import chat.common.handler.ChannelState;
import chat.common.listener.PacketAllCbListener;
import chat.common.main.Utils;
import chat.common.packet.Packet;
import io.netty.buffer.ByteBuf;

public class PacketAllCbSetState implements Packet<PacketAllCbListener> {

	public ChannelState cs = ChannelState.LOGIN;
	
	@Override
	public void decode(ByteBuf buf) {
		cs = ChannelState.valueOf(Utils.getString(buf));
	}

	@Override
	public void encode(ByteBuf buf) {
		Utils.writeString(buf, cs.name());
	}

	@Override
	public void process(PacketAllCbListener listener) {
		listener.process(this);
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return Utils.serializePacket(this);
	}
}
