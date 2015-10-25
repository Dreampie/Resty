package cn.dreampie.security;

/**
 * Created by ice on 14-12-23.
 */
public interface PasswordService {

  public String crypto(String password);

  public String crypto(String password, String salt);

  public boolean match(String password, String result);

  public boolean match(String password, String result, String salt);
}
