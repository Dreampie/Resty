package cn.dreampie.route.render;

import cn.dreampie.common.Render;
import cn.dreampie.common.http.ContentTypes;
import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;

/**
 * Created by ice on 14-12-29.
 */
public class TextRender extends Render {

  public void render(HttpRequest request, HttpResponse response, Object out) {
    if (out != null) {
      response.setContentType(ContentTypes.TEXT.toString());
      write(request, response, out.toString());
    }
  }

}
