package cn.dreampie.common.http;

/**
 * Created by wangrenhui on 15/4/29.
 */
public enum ContentTypes {

  TEXT("text/plain"),
  HTML("text/html"),
  XML("text/xml"),
  JSON("application/json"),
  JAVASCRIPT("application/javascript"),
  MULTIPART("multipart/form-data");

  private final String value;

  private ContentTypes(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }

  public String toString() {
    return value;
  }
}
