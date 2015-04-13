package cn.dreampie.route.render;

import cn.dreampie.common.Render;
import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;

import java.awt.image.RenderedImage;

/**
 * Created by ice on 14-12-29.
 */
public class ImageRender extends Render {

  private String type;

  public ImageRender(String type) {
    this.type = type;
  }

  public void render(HttpRequest request, HttpResponse response, Object out) {
    if (out != null) {
      if (out instanceof RenderedImage) {
        write(request, response, type, (RenderedImage) out);
      }
    }
  }

}
