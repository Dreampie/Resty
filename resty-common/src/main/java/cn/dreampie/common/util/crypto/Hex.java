package cn.dreampie.common.util.crypto;

import cn.dreampie.common.http.Encoding;

import java.io.UnsupportedEncodingException;

/**
 * @author Dreampie
 * @date 2015-10-22
 * @what
 */
public class Hex {


  /**
   * Default charset name is UTF_8
   *
   * @since 1.4
   */
  public static final String DEFAULT_CHARSET_NAME = Encoding.UTF_8.name();

  /**
   * Used to build output as Hex
   */
  private static final char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

  /**
   * Used to build output as Hex
   */
  private static final char[] DIGITS_UPPER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

  public static String encodeHexString(byte[] data) {
    return new String(encodeHex(data));
  }

  public static char[] encodeHex(byte[] data) {
    return encodeHex(data, true);
  }

  public static char[] encodeHex(byte[] data, boolean toLowerCase) {
    return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
  }

  protected static char[] encodeHex(byte[] data, char[] toDigits) {
    int l = data.length;
    char[] out = new char[l << 1];
    // two characters form the hex value.
    for (int i = 0, j = 0; i < l; i++) {
      out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
      out[j++] = toDigits[0x0F & data[i]];
    }
    return out;
  }
}
