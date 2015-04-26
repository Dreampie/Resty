package cn.dreampie.common.http.result;

import java.awt.image.RenderedImage;

/**
 * Created by ice on 14-12-19.
 */
public class ImageResult {

  private final String type;
  private final RenderedImage result;


  public ImageResult(String type, RenderedImage result) {
    this.type = type;
    this.result = result;
  }

  public ImageResult(RenderedImage result) {
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
  public RenderedImage getResult() {
    return result;
  }

}
