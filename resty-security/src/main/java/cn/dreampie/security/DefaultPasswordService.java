package cn.dreampie.security;

import cn.dreampie.common.util.crypto.Cryptor;

/**
 * Created by ice on 14-12-23.
 */
public class DefaultPasswordService implements PasswordService {
  private static PasswordService passwordService = new DefaultPasswordService();

  public static PasswordService instance() {
    return passwordService;
  }

  public String crypto(String password) {
    return Cryptor.crypto("SHA-512", password);
  }

  public String crypto(String password, String salt) {
    return Cryptor.crypto("SHA-512", password, salt);
  }

  public boolean match(String password, String result) {
    return crypto(password).equals(result);
  }

  public boolean match(String password, String result, String salt) {
    return crypto(password, salt).equals(result);
  }
}
