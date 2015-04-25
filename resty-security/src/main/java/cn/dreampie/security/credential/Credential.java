package cn.dreampie.security.credential;

import java.io.Serializable;

/**
 * Created by ice on 14-12-24.
 */
public class Credential implements Serializable {

  public static final String CREDENTIAL_DEF_KEY = "_credential";
  public static final String CREDENTIAL_ALL_KEY = "_allCredentials";

  private String httpMethod;
  private String antPath;
  private String value;

  public Credential(String httpMethod, String antPath, String value) {
    this.httpMethod = httpMethod.toUpperCase();
    this.antPath = antPath;
    this.value = value;
  }

  public String getHttpMethod() {
    return httpMethod;
  }

  public void setHttpMethod(String httpMethod) {
    this.httpMethod = httpMethod;
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
