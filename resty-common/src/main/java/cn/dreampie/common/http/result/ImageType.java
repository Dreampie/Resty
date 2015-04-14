package cn.dreampie.common.http.result;

/**
 * Created by ice on 14-12-19.
 */
public enum ImageType {

  PNG("png"), JPG("jpg"), GIF("gif"), BMP("bmp");

  private final String type;

  private ImageType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

}
