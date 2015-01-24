package cn.dreampie.common.util;

/**
 * Created by wangrenhui on 15/1/23.
 */
public class Stringer {

  public static String firstLower(String name) {
    byte[] items = name.getBytes();
    items[0] = (byte) ((char) items[0] + ('a' - 'A'));
    return new String(items);
  }

  public static String firstUpper(String name) {
    byte[] items = name.getBytes();
    items[0] = (byte) ((char) items[0] + ('A' - 'a'));
    return new String(items);
  }
}
