package chat.common.work;

import static java.security.KeyFactory.getInstance;
import static java.util.Base64.getEncoder;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class RSAUtils {
	public static int key_length = 4096;
	public static KeyPair genKey() {
		try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			SecureRandom sr = new SecureRandom();
			kpg.initialize(key_length, sr);
			KeyPair kp = kpg.generateKeyPair();
			return kp;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	public static PrivateKey genPrivateKey(byte[] key) {
		try {
			return getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(key));
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	public static PublicKey genPublicKey(byte[] key) {
		try {
			return getInstance("RSA").generatePublic(new X509EncodedKeySpec(key));
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static byte[] encrypt(byte[] data, Key k) {
		try {
			Cipher c = Cipher.getInstance("RSA");
			c.init(Cipher.ENCRYPT_MODE, k);
			return c.doFinal(data);
		} catch (NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
			throw new RuntimeException(e);
		}
	}
	public static byte[] decrypt(byte[] data, Key k) {
		try {
			Cipher c = Cipher.getInstance("RSA");
			c.init(Cipher.DECRYPT_MODE, k);
			return c.doFinal(data);
		} catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			throw new RuntimeException(e);
		}
	}
	public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException {
		KeyPair k = genKey();
		String encrypted = getEncoder().encodeToString(encrypt("Hello, world!".getBytes(), k.getPrivate()));
		System.out.println(encrypted);
		System.out.println(new String(decrypt(Base64.getDecoder().decode(encrypted), k.getPublic())));
		
	}
}
