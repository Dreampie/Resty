package cn.dreampie.common.http.result;

import cn.dreampie.common.http.ContentType;

import java.awt.image.RenderedImage;
import java.util.Map;

/**
 * Created by ice on 14-12-19.
 */
public class ImageResponse<T extends RenderedImage> extends HttpResponse<T> {

  private final String type;

  public ImageResponse(HttpStatus status, String type) {
    super(status);
    this.type = type;
  }

  public ImageResponse(HttpStatus status, String type, Map<String, String> headers) {
    super(status, headers);
    this.type = type;
  }

  public ImageResponse(T result) {
    super(result);
    this.type = ContentType.PNG.type();
  }

  public ImageResponse(T result, String type) {
    super(result);
    this.type = type;
  }

  public ImageResponse(T result, String type, Map<String, String> headers) {
    super(result, headers);
    this.type = type;
  }

  public ImageResponse(HttpStatus status, T result, String type) {
    super(status, result);
    this.type = type;
  }

  public ImageResponse(HttpStatus status, T result, String type, Map<String, String> headers) {
    super(status, result, headers);
    this.type = type;
  }

  public String getType() {
    return type;
  }

}
