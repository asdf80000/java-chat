package chat.common.packet.all;

import com.google.gson.Gson;

import chat.common.listener.PacketAllCbListener;
import chat.common.main.Utils;
import chat.common.packet.Packet;
import io.netty.buffer.ByteBuf;

public class PacketAllCbMessage implements Packet<PacketAllCbListener> {

	public PacketAllCbMessageType type;
	public String[] array;

	public PacketAllCbMessage(PacketAllCbMessageType type, String[] arr) {
		this.type = type;
		this.array = arr;
	}

	public PacketAllCbMessage(PacketAllCbMessageType type) {
		this.type = type;
		this.array = new String[] {};
	}

	public PacketAllCbMessage() {
		this.type = PacketAllCbMessageType.NULL;
		this.array = new String[] {};
	}

	@Override
	public void decode(ByteBuf buf) {
		type = PacketAllCbMessageType.valueOf(Utils.getString(buf));
		array = new Gson().fromJson(Utils.getString(buf), String[].class);
	}

	@Override
	public void encode(ByteBuf buf) {
		Utils.writeString(buf, type.name());
		Utils.writeString(buf, new Gson().toJson(array));
	}

	@Override
	public void process(PacketAllCbListener listener) {
		listener.process(this);
	}

	public static enum PacketAllCbMessageType {
		NULL, MATCHSTARTED, NICKCHANGED, HELLO, EXITED, MATCHFOUND, YOURNICK;
	}
}
