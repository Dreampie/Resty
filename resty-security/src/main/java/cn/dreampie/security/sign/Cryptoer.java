package cn.dreampie.security.sign;

import cn.dreampie.common.http.Charsets;
import cn.dreampie.security.encode.BASE64Encoder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by ice on 14-12-23.
 */
public class Cryptoer {

  private static final BASE64Encoder base64Encoder = new BASE64Encoder();

  /**
   * Sign a message with a key
   *
   * @param message The message to sign
   * @param key     The key to use
   * @return The signed message (in hexadecimal)
   */
  public static String sign(String message, byte[] key) {
    if (key.length == 0) {
      return message;
    }

    try {
      Mac mac = Mac.getInstance("HmacSHA1");
      SecretKeySpec signingKey = new SecretKeySpec(key, "HmacSHA1");
      mac.init(signingKey);
      byte[] messageBytes = message.getBytes(Charsets.UTF_8);
      byte[] result = mac.doFinal(messageBytes);
      return base64Encoder.encode(result);
    } catch (Exception ex) {
      throw new RuntimeException(ex.getMessage(), ex);
    }
  }
}
