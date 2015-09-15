/**
 * Copyright (c) 2011-2015, James Zhan 詹波 (jfinal@126.com).
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
      if (salt != null) {
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




