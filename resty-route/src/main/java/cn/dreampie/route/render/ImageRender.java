package cn.dreampie.route.render;

import cn.dreampie.common.Render;
import cn.dreampie.common.http.ContentType;
import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.common.http.exception.WebException;
import cn.dreampie.common.http.result.HttpStatus;
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
      ImageResult<RenderedImage> result = null;
      if (out instanceof RenderedImage) {
        result = new ImageResult<RenderedImage>((RenderedImage) out);
      }

      if (result == null) {
        throw new WebException(HttpStatus.NOT_FOUND, "Image not support '" + out + "'.");
      } else {
        response.setContentType(ContentType.typeOf(result.getType()).value());
        write(request, response, result.getType(), result.getResult());
      }
    }
  }
}
