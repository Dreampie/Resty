package cn.dreampie.common.http.result;

import java.awt.image.RenderedImage;

/**
 * Created by ice on 14-12-19.
 */
public class ImageResult<T extends RenderedImage> {

  private final String type;
  private final T result;


  public ImageResult(String type, T result) {
    this.type = type;
    this.result = result;
  }

  public ImageResult(T result) {
    this.type = "png";
    this.result = result;
  }

  public String getType() {
    return type;
  }

  /**
   * Returns the content to use in the HTTP response .
   *
   * @return the content to use in the response.
   */
  public T getResult() {
    return result;
  }

}
