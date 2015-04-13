package cn.dreampie.route.render;

import cn.dreampie.common.Render;
import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.log.Logger;

import java.awt.image.RenderedImage;

/**
 * Created by ice on 14-12-29.
 */
public class ImageRender extends Render {
  private static final Logger logger = Logger.getLogger(ImageRender.class);

  private String type;

  public ImageRender(String type) {
    this.type = type;
  }

  public void render(HttpRequest request, HttpResponse response, Object out) {
    if (out != null) {
      if (out instanceof RenderedImage) {
        write(request, response, type, (RenderedImage) out);
      } else {
        logger.warn("Image render object isn't a image.");
      }
    }
  }

}
