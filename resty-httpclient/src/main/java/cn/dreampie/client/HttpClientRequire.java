package cn.dreampie.client;

import java.net.HttpURLConnection;

/**
 * @author Dreampie
 * @date 2015-09-14
 * @what
 */
public class HttpClientRequire {

  /**
   * 重新登录的条件
   *
   * @param result
   * @return
   */
  public boolean relogin(HttpClientResult result) {
    return result.getStatus().getCode() == HttpURLConnection.HTTP_UNAUTHORIZED;
  }
}
