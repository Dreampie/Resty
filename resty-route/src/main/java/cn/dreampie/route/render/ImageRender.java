package cn.dreampie.route.render;

import cn.dreampie.common.Render;
import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.common.http.result.ImageResult;
import cn.dreampie.log.Logger;

import java.awt.image.RenderedImage;

/**
 * Created by ice on 14-12-29.
 */
public class ImageRender extends Render {
  private static final Logger logger = Logger.getLogger(ImageRender.class);

  public void render(HttpRequest request, HttpResponse response, Object out) {
    if (out != null) {
      if (out instanceof RenderedImage) {
        ImageResult result = new ImageResult((RenderedImage) out);
        write(request, response, result.getImageType(), result.getResult());
      } else if (out instanceof ImageResult) {
        ImageResult result = (ImageResult) out;
        write(request, response, result.getImageType(), result.getResult());
      } else {
        logger.warn("Image render object isn't a image.");
      }
    }
  }
}
