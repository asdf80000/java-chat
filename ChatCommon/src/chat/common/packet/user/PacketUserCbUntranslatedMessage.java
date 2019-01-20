package chat.common.packet.user;

import chat.common.listener.PacketUserCbListener;
import chat.common.main.Utils;
import chat.common.packet.Packet;
import io.netty.buffer.ByteBuf;

public class PacketUserCbUntranslatedMessage implements Packet<PacketUserCbListener> {

	public PacketUserCbUntranslatedMessage(String text) {
		this();
		this.message = text;
	}
	public PacketUserCbUntranslatedMessage() {
		this.message = "null";
	}
	public String message;
	@Override
	public void decode(ByteBuf buf) {
		message = Utils.getString(buf);
	}

	@Override
	public void encode(ByteBuf buf) {
		Utils.writeString(buf, message);
	}

	@Override
	public void process(PacketUserCbListener listener) {
		listener.process(this);
	}

}
