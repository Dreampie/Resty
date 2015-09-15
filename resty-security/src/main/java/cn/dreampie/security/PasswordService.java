package cn.dreampie.security;

/**
 * Created by ice on 14-12-23.
 */
public interface PasswordService {

  public String hash(String password);

  public String hash(String password, String salt);

  public boolean match(String password, String passwordHash);

  public boolean match(String password, String passwordHash, String salt);
}
