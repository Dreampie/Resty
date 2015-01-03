package cn.dreampie.route.render;

import cn.dreampie.route.base.Render;
import cn.dreampie.route.http.HttpRequest;
import cn.dreampie.route.http.HttpResponse;
import cn.dreampie.util.HttpTyper;

/**
 * Created by ice on 14-12-29.
 */
public class TextRender extends Render {
  public void render(HttpRequest request, HttpResponse response, Object out) {
    response.setContentType(HttpTyper.ContentType.TEXT.toString());
    if (out == null) {
      write(request, response, "");
    } else {
      write(request, response, out.toString());
    }
  }
}
