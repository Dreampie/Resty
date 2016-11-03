package cn.dreampie.common.http.result;

import cn.dreampie.common.http.ContentType;

import javax.servlet.http.Cookie;
import java.awt.image.RenderedImage;
import java.util.List;
import java.util.Map;

/**
 * Created by ice on 14-12-19.
 */
public class ImageResult<T extends RenderedImage> extends HttpResult<T> {

  private final String type;

  public ImageResult(HttpStatus status, String type) {
    super(status);
    this.type = type;
  }

  public ImageResult(HttpStatus status, String type, Map<String, String> headers) {
    super(status, headers);
    this.type = type;
  }

  public ImageResult(HttpStatus status, String type, List<Cookie> cookies) {
    super(status, cookies);
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

  public ImageResult(T result, String type, List<Cookie> cookies) {
    super(result, cookies);
    this.type = type;
  }

  public ImageResult(HttpStatus status, T result, String type) {
    super(status, result);
    this.type = type;
  }

  public ImageResult(HttpStatus status, T result, String type, List<Cookie> cookies) {
    super(status, result, cookies);
    this.type = type;
  }

  public ImageResult(HttpStatus status, T result, String type, Map<String, String> headers) {
    super(status, result, headers, null);
    this.type = type;
  }

  public ImageResult(HttpStatus status, T result, String type, Map<String, String> headers, List<Cookie> cookies) {
    super(status, result, headers, cookies);
    this.type = type;
  }

  public String getType() {
    return type;
  }

}
