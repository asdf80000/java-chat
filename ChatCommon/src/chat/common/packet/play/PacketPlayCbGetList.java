package chat.common.packet.play;

import java.util.ArrayList;

import chat.common.listener.PacketPlayCbListener;
import chat.common.main.Utils;
import chat.common.packet.Packet;
import io.netty.buffer.ByteBuf;

public class PacketPlayCbGetList implements Packet<PacketPlayCbListener> {

	public ArrayList<byte[]> list;

	public int getLength() {
		return list.size();
	}

	@Override
	public void decode(ByteBuf buf) {
		int n = buf.readInt();
		list = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			list.add(Utils.getByteArray(buf));
		}
	}

	@Override
	public void encode(ByteBuf buf) {
		buf.writeInt(list.size());
		for (int i = 0; i < list.size(); i++) {
			Utils.writeByteArray(buf, list.get(i));
		}
	}

	@Override
	public void process(PacketPlayCbListener listener) {
		listener.process(this);
	}

}
