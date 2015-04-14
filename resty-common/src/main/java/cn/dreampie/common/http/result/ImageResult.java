package cn.dreampie.common.http.result;

import java.awt.image.RenderedImage;

/**
 * Created by ice on 14-12-19.
 */
public class ImageResult {

  private final ImageType imageType;
  private final RenderedImage result;

  public ImageResult(ImageType imageType) {
    this.imageType = imageType;
    this.result = null;
  }

  public ImageResult(ImageType imageType, RenderedImage result) {
    this.imageType = imageType;
    this.result = result;
  }

  public ImageResult(RenderedImage result) {
    this.imageType = ImageType.PNG;
    this.result = result;
  }

  public ImageType getImageType() {
    return imageType;
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
