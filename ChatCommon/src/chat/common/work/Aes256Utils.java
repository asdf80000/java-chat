package chat.common.work;

import static javax.crypto.Cipher.DECRYPT_MODE;
import static javax.crypto.Cipher.ENCRYPT_MODE;
import static javax.crypto.Cipher.getInstance;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Aes256Utils {
	public static int key_length = 256;
	public static SecretKey genKey() {
		try {
			KeyGenerator kg = KeyGenerator.getInstance("AES");
			kg.init(key_length);
			return kg.generateKey();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	public static byte[] encrypt(byte[] data, Key k) {
		try {
			Cipher c = getInstance("AES");
			c.init(ENCRYPT_MODE, k);
			return c.doFinal(data);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	public static byte[] decrypt(byte[] data, Key k) {
		try {
			Cipher c = getInstance("AES");
			c.init(DECRYPT_MODE, k);
			return c.doFinal(data);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	public static SecretKey genKey(byte[] key) {
		SecretKeySpec sks = new SecretKeySpec(key, "AES");
		return sks;
	}
}
