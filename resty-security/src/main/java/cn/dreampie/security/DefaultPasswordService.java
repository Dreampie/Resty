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

  public String hash(String password) {
    return Cryptor.crypto("HmacSHA512", password);
  }

  public String hash(String password, String salt) {
    return Cryptor.crypto("HmacSHA512", password, salt);
  }

  public boolean match(String password, String passwordHash) {
    return hash(password).equals(passwordHash);
  }

  public boolean match(String password, String passwordHash, String salt) {
    return hash(password, salt).equals(passwordHash);
  }
}
