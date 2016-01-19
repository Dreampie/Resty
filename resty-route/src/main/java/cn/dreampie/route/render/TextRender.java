package cn.dreampie.route.render;

import cn.dreampie.common.Render;
import cn.dreampie.common.http.ContentType;
import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;

/**
 * Created by ice on 14-12-29.
 */
public class TextRender extends Render {

  public void render(HttpRequest request, HttpResponse response, Object out) {
    if (out != null) {
      response.setContentType(ContentType.TEXT.value());
      write(request, response, out.toString());
    }
  }

}
