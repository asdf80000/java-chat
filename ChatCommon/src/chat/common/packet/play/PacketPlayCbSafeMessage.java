package chat.common.packet.play;

import javax.crypto.SecretKey;

import com.google.gson.Gson;

import chat.common.listener.PacketPlayCbListener;
import chat.common.main.Utils;
import chat.common.packet.Packet;
import chat.common.work.Aes256Utils;
import io.netty.buffer.ByteBuf;

public class PacketPlayCbSafeMessage implements Packet<PacketPlayCbListener> {

	private byte[] _type;
	private byte[] _array;

	public PacketPlayCbSafeMessageType type;
	public String[] array;

	private SecretKey _aes;

	public void setAesWritingKey(SecretKey aesKey) {
		this._aes = aesKey;
	}

	@Override
	public void decode(ByteBuf buf) {
		_type = Utils.getByteArray(buf);
		_array = Utils.getByteArray(buf);
	}

	public void decodeAfter(SecretKey aesKey) {
		String str = Utils.byteStr(Aes256Utils.decrypt(_type, aesKey));
		type = PacketPlayCbSafeMessageType.valueOf(str);
		str = Utils.byteStr(Aes256Utils.decrypt(_array, aesKey));
		array = new Gson().fromJson(str, String[].class);
	}

	@Override
	public void encode(ByteBuf buf) {
		System.out.println("PAES key : " + _aes);
		String name = type.name();
		Utils.writeByteArray(buf, Aes256Utils.encrypt(Utils.byteStr(name), _aes));
		if (array == null)
			array = new String[0];
		String json = new Gson().toJson(array);
		Utils.writeByteArray(buf, Aes256Utils.encrypt(Utils.byteStr(json), _aes));
	}

	public static enum PacketPlayCbSafeMessageType { // @formatter:off
		NULL, EXITED, JOINED,
		STILL_0, STILL_1, STILL_2,
		MATCHINFO_0, MATCHINFO_1, MATCHINFO_2, MATCHINFO_3,
		MSG_TOO_LONG, MSG_TOO_SHORT;
	}// @formatter:on

	@Override
	public void process(PacketPlayCbListener listener) {
		listener.process(this);
	}

	@Override
	public String toString() {
		return "Server's safe message";
	}
}
