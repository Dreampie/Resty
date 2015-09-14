package cn.dreampie.client;

import java.net.HttpURLConnection;

/**
 * @author Dreampie
 * @date 2015-09-14
 * @what
 */
public class ClientRequire {

  /**
   * 重新登录的条件
   *
   * @param result
   * @return
   */
  public boolean relogin(ClientResult result) {
    return result.getStatus().getCode() == HttpURLConnection.HTTP_UNAUTHORIZED;
  }
}
