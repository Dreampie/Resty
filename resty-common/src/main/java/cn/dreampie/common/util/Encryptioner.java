package cn.dreampie.common.util;

import cn.dreampie.common.http.Encoding;

import java.security.MessageDigest;

public class Encryptioner {

  public static String md5(String srcStr) {
    return encrypt("MD5", srcStr, null);
  }

  public static String md5(String srcStr, String salt) {
    return encrypt("MD5", srcStr, salt);
  }

  public static String sha1(String srcStr) {
    return encrypt("SHA-1", srcStr, null);
  }

  public static String sha1(String srcStr, String salt) {
    return encrypt("SHA-1", srcStr, salt);
  }

  public static String sha256(String srcStr) {
    return encrypt("SHA-256", srcStr, null);
  }

  public static String sha256(String srcStr, String salt) {
    return encrypt("SHA-256", srcStr, salt);
  }

  public static String sha384(String srcStr) {
    return encrypt("SHA-384", srcStr, null);
  }

  public static String sha384(String srcStr, String salt) {
    return encrypt("SHA-384", srcStr, salt);
  }

  public static String sha512(String srcStr) {
    return encrypt("SHA-512", srcStr, null);
  }

  public static String sha512(String srcStr, String salt) {
    return encrypt("SHA-512", srcStr, salt);
  }

  private static String encrypt(String algorithm, String srcStr, String salt) {
    try {
      StringBuilder result = new StringBuilder();
      MessageDigest md = MessageDigest.getInstance(algorithm);
      if (salt != null && !salt.isEmpty()) {
        md.update(salt.getBytes(Encoding.UTF_8));
      }
      byte[] bytes = md.digest(srcStr.getBytes(Encoding.UTF_8));
      for (byte b : bytes) {
        String hex = Integer.toHexString(b & 0xFF);
        if (hex.length() == 1)
          result.append("0");
        result.append(hex);
      }
      return result.toString();
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}




