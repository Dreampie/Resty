package cn.dreampie.security;

/**
 * Created by wangrenhui on 14/12/23.
 */
public class Session {
  public static final String SESSION_DEF_KEY = "sessionKey";
  private String key;
  private Principal principal;

  public Session(String key, Principal principal) {
    this.key = key;
    this.principal = principal;
  }

  public String getKey() {
    return key;
  }

  public Principal getPrincipal() {
    return principal;
  }
}
