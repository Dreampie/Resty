package cn.dreampie.client;

/**
 * Created by wangrenhui on 15/1/11.
 */
public class ResponseData {
  private int httpCode;
  private String data;

  public ResponseData(int httpCode, String data) {
    this.httpCode = httpCode;
    this.data = data;
  }

  public int getHttpCode() {
    return httpCode;
  }

  public String getData() {
    return data;
  }

  public String toString() {
    return "ResponseData{" +
        "httpCode=" + httpCode +
        ", data='" + data + '\'' +
        '}';
  }
}
