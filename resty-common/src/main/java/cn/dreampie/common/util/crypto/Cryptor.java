package cn.dreampie.common.util.crypto;

import cn.dreampie.common.http.Encoding;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Cryptor {

  public static final String MD2 = "MD2";
  public static final String MD5 = "MD5";

  public static final String SHA1 = "SHA-1";
  public static final String SHA256 = "SHA-256";
  public static final String SHA384 = "SHA-384";
  public static final String SHA512 = "SHA-512";

  public static final String HMACMD5 = "HmacMD5";
  public static final String HMACSHA1 = "HmacSHA1";
  public static final String HMACSHA256 = "HmacSHA256";
  public static final String HMACSHA384 = "HmacSHA384";
  public static final String HMACSHA512 = "HmacSHA512";

  private static List<String> mds = new ArrayList<String>() {{
    add(MD2);
//    add("MD4"); Bouncy Castle
    add(MD5);
  }};

  private static List<String> shas = new ArrayList<String>() {{
    add(SHA1);
//    add("SHA-224"); Bouncy Castle
    add(SHA256);
    add(SHA384);
    add(SHA512);
  }};

  private static List<String> macs = new ArrayList<String>() {{
//    add("HmacMD2");  Bouncy Castle
//    add("HmacMD4"); Bouncy Castle
    add(HMACMD5);
    add(HMACSHA1);
//    add("HmacSHA224");Bouncy Castle
    add(HMACSHA256);
    add(HMACSHA384);
    add(HMACSHA512);
  }};

  public static String crypto(String algorithm, String message) {
    return crypto(algorithm, message, null);
  }

  public static String crypto(String algorithm, String message, String salt) {
    String result;
    if (macs.contains(algorithm)) {
      Mac mac = null;
      try {
        mac = Mac.getInstance(algorithm);
        if (salt != null && !salt.isEmpty()) {
          SecretKeySpec secretKey = new SecretKeySpec(salt.getBytes(Encoding.UTF_8), algorithm);
          mac.init(secretKey);
        } else {
          throw new CryptoException("Could not found secretKey for mac's crypto.");
        }
      } catch (GeneralSecurityException e) {
        throw new CryptoException(e.getMessage(), e);
      }
      result = Hex.encodeHexString(mac.doFinal(message.getBytes(Encoding.UTF_8)));
    } else if (mds.contains(algorithm) || shas.contains(algorithm)) {
      MessageDigest md = null;
      try {
        md = MessageDigest.getInstance(algorithm);
      } catch (NoSuchAlgorithmException e) {
        throw new CryptoException(e.getMessage(), e);
      }

      if (salt != null && !salt.isEmpty()) {
        md.update(salt.getBytes(Encoding.UTF_8));
      }
      result = Hex.encodeHexString(md.digest(message.getBytes()));
    } else {
      throw new CryptoException("Could not support this crypto's type " + algorithm + ".");
    }

    return result;
  }
}




