package cn.dreampie.common.http.result;

import cn.dreampie.common.http.ContentType;

import java.awt.image.RenderedImage;
import java.util.Map;

/**
 * Created by ice on 14-12-19.
 */
public class ImageResult<T extends RenderedImage> extends WebResult<T> {

  private final String type;

  public ImageResult(HttpStatus status, String type) {
    super(status);
    this.type = type;
  }

  public ImageResult(HttpStatus status, String type, Map<String, String> headers) {
    super(status, headers);
    this.type = type;
  }

  public ImageResult(T result) {
    super(result);
    this.type = ContentType.PNG.type();
  }

  public ImageResult(T result, String type) {
    super(result);
    this.type = type;
  }

  public ImageResult(T result, String type, Map<String, String> headers) {
    super(result, headers);
    this.type = type;
  }

  public ImageResult(HttpStatus status, T result, String type) {
    super(status, result);
    this.type = type;
  }

  public ImageResult(HttpStatus status, T result, String type, Map<String, String> headers) {
    super(status, result, headers);
    this.type = type;
  }

  public String getType() {
    return type;
  }

}
