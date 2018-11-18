package chat.common.main;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;

import org.apache.commons.text.StringEscapeUtils;

import chat.common.packet.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

public class Utils {
	public static <T> AttributeKey<T> newAttributeKey(Class<T> clazz, String name){
		return AttributeKey.newInstance(name);
	}
	public static String randomStr(int len) {
		String allowed = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String str = "";
		for(int i=0;i<len;i++) {
			int r = (int)(Math.random() * allowed.length());
			str += allowed.substring(r, r+1);
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
			//throw new RuntimeException("Your computer is too crazy.");
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
			for(Field f : p.getClass().getFields()) {
				str += "\"" + f.getName() + "\": ";
				str += serialize(f.get(p));
				if(first) first = false;
				else str += ", ";
			}
			str += "}";
			return str;
		} catch (IllegalAccessException e) {
			return "{Serialize Error}";
		}
	}
	public static String serialize(Object obj) {
		if(obj instanceof String) {
			return "\"" + StringEscapeUtils.escapeJava((String) obj) + "\"";
		}else if(obj instanceof Packet<?>) {
			return obj.toString();
		}else if(obj instanceof Integer) {
			return obj.toString();
		}else if(obj instanceof Double) {
			return obj.toString() + "d";
		}else if(obj instanceof Float) {
			return obj.toString() + "f";
		}else if(obj instanceof Enum) {
			System.out.println("this" + obj.getClass());
			//((Enum) obj).getDeclaringClass().getSimpleName()
			return ((Enum<?>) obj).getDeclaringClass().getSimpleName() + "." + obj;
		}
		return obj.toString();
	}
}
