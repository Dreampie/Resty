package cn.dreampie.core.render;

import cn.dreampie.core.base.Render;
import cn.dreampie.core.http.HttpRequest;
import cn.dreampie.core.http.HttpResponse;
import cn.dreampie.kit.HttpTyper;

/**
 * Created by ice on 14-12-29.
 */
public class TextRender extends Render {
  public void render(HttpRequest request, HttpResponse response, Object out) {
    response.setContentType(HttpTyper.ContentType.TEXT.toString());
    write(request, response, out.toString());
  }
}
