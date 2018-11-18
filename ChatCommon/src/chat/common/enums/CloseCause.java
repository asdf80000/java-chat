package chat.common.enums;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import io.netty.buffer.ByteBuf;

public enum CloseCause {
	DISCONNECT(0);
	private static BiMap<CloseCause, Integer> bm = HashBiMap.create();
	private int id = 0;
	private CloseCause(int id) {
		this.id = id;
	}
	static {
		for(CloseCause cc : values()) {
			bm.put(cc, cc.id);
		}
	}
	public void encode(ByteBuf buf) {
		buf.writeInt(id);
	}
	public static CloseCause decode(ByteBuf buf) {
		return bm.inverse().get(buf.readInt());
	}
}
