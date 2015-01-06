package cn.dreampie.route.render;

import cn.dreampie.common.Render;
import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.common.util.HttpTyper;

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
