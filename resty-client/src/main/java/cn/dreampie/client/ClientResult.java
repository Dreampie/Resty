package cn.dreampie.client;

import cn.dreampie.common.http.result.HttpStatus;
import cn.dreampie.common.util.json.Jsoner;

/**
 * Created by wangrenhui on 15/1/11.
 */
public class ClientResult {
  private HttpStatus status;
  private String result;

  public ClientResult(HttpStatus status, String result) {
    this.status = status;
    this.result = result;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public String getResult() {
    return result;
  }

  public <T> T getResult(Class<? extends T> clazz) {
    return Jsoner.toObject(result, clazz);
  }

  public String toString() {
    return "ClientResult{" +
        "status=" + status +
        ", result='" + result + '\'' +
        '}';
  }
}
