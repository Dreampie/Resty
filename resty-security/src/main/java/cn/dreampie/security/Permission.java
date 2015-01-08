package cn.dreampie.security;

/**
 * Created by ice on 14-12-24.
 */
public class Permission {

  public static final String PERMISSION_DEF_KEY = "_permission";
  public static final String PERMISSION_ALL_KEY = "_allPermission";

  private String method;
  private String antPath;
  private String value;

  public Permission(String method, String antPath, String value) {
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
