package cn.dreampie.common.http.result;

/**
 * Created by ice on 14-12-19.
 */
public enum ImageType {

  PNG("png"), JPG("jpg"), GIF("gif"), BMP("bmp");

  private final String value;

  private ImageType(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }

}
