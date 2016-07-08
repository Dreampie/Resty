package cn.dreampie.client;

import cn.dreampie.common.util.Maper;

import java.util.Map;

/**
 * @author Dreampie
 * @date 2015-09-14
 * @what
 */
public class HttpClientUser {
  private String username;
  private String password;
  private boolean rememberMe;

  public HttpClientUser(String username, String password, boolean rememberMe) {
    this.username = username;
    this.password = password;
    this.rememberMe = rememberMe;
  }

  /**
   * 如果你是自定义的参数名，继承并修改该方法
   *
   * @return
   */
  public Map<String, String> reserveMap() {
    return Maper.of("username", username, "password", password, "rememberMe", Boolean.toString(rememberMe));
  }

}
