package cn.dreampie.security;

/**
 * Created by ice on 14-12-24.
 */
public class Credential {

  public static final String CREDENTIAL_DEF_KEY = "_credential";
  public static final String CREDENTIAL_ALL_KEY = "_allCredentials";

  private String method;
  private String antPath;
  private String value;

  public Credential(String method, String antPath, String value) {
    this.method = method.toUpperCase();
    this.antPath = antPath;
    this.value = value;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public String getAntPath() {
    return antPath;
  }

  public void setAntPath(String antPath) {
    this.antPath = antPath;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
