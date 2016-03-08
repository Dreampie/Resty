package cn.dreampie.common.http;

/**
 * Created by wangrenhui on 15/4/29.
 */
public enum ContentType {

  TEXT("text", "text/plain"),
  HTML("html", "text/html"),
  XML("xml", "text/xml"),
  JSON("json", "application/json"),
  FORM("form", "application/x-www-form-urlencoded"),
  MULTIPART("multipart", "multipart/form-data"),
  FILE("file", "application/octet-stream"),
  PNG("png", "image/png"),
  JPEG("jpg", "image/jpeg"),
  GIF("gif", "image/gif");

  private String type;
  private String value;

  private ContentType(String type, String value) {
    this.type = type;
    this.value = value;
  }

  public static ContentType typeOf(String type) {
    for (ContentType contentType : values()) {
      if (contentType.type.equals(type)) {
        return contentType;
      }
    }
    throw new IllegalArgumentException("Invalid Content-Type: " + type);
  }

  public String type() {
    return type;
  }

  public String value() {
    return value;
  }

}
