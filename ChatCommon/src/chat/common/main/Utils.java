package chat.common.main;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import org.apache.commons.text.StringEscapeUtils;

import com.google.common.reflect.TypeToken;

import chat.common.packet.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

public class Utils {
	public static <T> AttributeKey<T> newAttributeKey(Class<T> clazz, String name) {
		return AttributeKey.newInstance(name);
	}

	public static String randomStr(int len) {
		String allowed = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String str = "";
		for (int i = 0; i < len; i++) {
			int r = (int) (Math.random() * allowed.length());
			str += allowed.substring(r, r + 1);
		}
		return str;
	}

	public static byte[] getByteArray(ByteBuf b) {
		try {
			int n = b.readInt();
			byte[] bs = new byte[n];
			b.readBytes(bs);
			return bs;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void writeByteArray(ByteBuf b, byte[] data) {
		b.writeInt(data.length);
		b.writeBytes(data);
	}

	public static String getString(ByteBuf b) {
		try {
			int len = b.readInt();
			byte[] bs = new byte[len];
			b.readBytes(bs);

			return new String(bs, "UTF8");
		} catch (Exception e) {
			e.printStackTrace();
			// throw new RuntimeException("Your computer is too crazy.");
			return null;
		}
	}

	public static void writeString(ByteBuf b, String text) {
		try {
			byte[] bs = text.getBytes("UTF8");
			b.writeInt(bs.length);
			b.writeBytes(bs);
		} catch (UnsupportedEncodingException e) {
			// TODO: handle exception
			throw new RuntimeException("Your computer is too crazy.");
		}
	}

	public static <T> Attribute<T> getChannelAttr(AttributeKey<T> key, Channel ch) {
		return ch.attr(key);
	}

	public static boolean isServer = true;

	public static String serializePacket(Packet<?> p) {
		try {
			String str = "{";
			boolean first = true;
			for (Field f : p.getClass().getFields()) {
				if (first)
					first = false;
				else
					str += ", ";
				str += "\"" + f.getName() + "\": ";
				str += serialize(f.get(p));
			}
			str += "}";
			return str;
		} catch (IllegalAccessException e) {
			return "{Serialize Error}";
		}
	}

	public static String serialize(Object obj) {
		if (obj == null) // null threw error before. So added null checker
			return "null";
		if (obj instanceof String) {
			return "\"" + StringEscapeUtils.escapeJava((String) obj) + "\"";
		} else if (obj instanceof Packet<?>) {
			return serializePacket((Packet<?>) obj);
		} else if (obj instanceof Integer) {
			return obj.toString();
		} else if (obj instanceof Double) {
			return obj.toString() + "d";
		} else if (obj instanceof Float) {
			return obj.toString() + "f";
		} else if (obj instanceof Enum) {
			return ((Enum<?>) obj).getDeclaringClass().getSimpleName() + "." + obj;
		} else if (obj instanceof SecretKey) {
			return "Secret AES key";
		} else if (obj instanceof PublicKey) {
			return "Public RSA key";
		} else if (obj instanceof PrivateKey) {
			System.out.println(
					"[Security warning] PRIVATE RSA KEY DETECTED IN PACKET. SERVER MIGHT BE HACKED. DEBUG STACK TRACE BELOW.");
			new Exception().printStackTrace();
			return "Private RSA key";
		} else if (obj.getClass().isArray()) {
			return obj.getClass().getComponentType().getName() + "[" + Array.getLength(obj) + "]";
		}
		return obj.toString();
	}

	public static String byteStr(byte[] str) {
		try {
			return new String(str, "UTF8");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] byteStr(String str) {
		try {
			return str.getBytes("UTF8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static void sleep() {
		try {
			Thread.sleep(100);
		} catch (Exception e) {
		}
	}

	public static void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
		}
	}

	public static boolean log = true;

	public static <T> Type getType(Class<T> clazz) {
		return new TypeToken<T>() {
			private static final long serialVersionUID = 1L;
		}.getType();
	}

	public static boolean isFilenameValid(String file) {
		File f = new File(file);
		try {
			f.getCanonicalPath();
			return true;
		} catch (IOException e) {
			return false;
		}
	}
}
