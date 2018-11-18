package chat.common.work;

import static java.util.Base64.getDecoder;
import static java.util.Base64.getEncoder;

import java.io.UnsupportedEncodingException;

public class B64Utils {
	public static byte[] B64ToDat(String base64) {
		return getDecoder().decode(base64);
	}
	public static String DatToB64(byte[] base64) {
		return getEncoder().encodeToString(base64);
	}
	public static String B64ToStr(String base64) throws UnsupportedEncodingException {
		return new String(getDecoder().decode(base64), "UTF8");
	}
	public static String StrToB64(String base64) throws UnsupportedEncodingException {
		return getEncoder().encodeToString(base64.getBytes("UTF8"));
	}
}
