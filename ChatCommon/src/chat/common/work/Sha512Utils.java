package chat.common.work;

import java.security.MessageDigest;

public class Sha512Utils {
	public static String shaencode(String data) {
		try {
			return shaencode(data.getBytes("UTF8"));
		} catch (Exception e) {
			return null;
		}
	}
	public static String shaencode(byte[] data) {
		try {
			return shaencode_(data);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	private static String shaencode_(byte[] data) throws Exception {
		MessageDigest md = MessageDigest.getInstance("SHA-512");
		md.reset();
		md.update(data);
		return byteToHex(md.digest());
	}
	public static String byteToHex(byte[] data) {
		StringBuilder sb = new StringBuilder();
		for(byte b : data) {
			sb.append(String.format("%02X", b));
		}
		return sb.toString();
	}
}
