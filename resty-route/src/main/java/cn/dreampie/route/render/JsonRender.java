package cn.dreampie.route.render;

import cn.dreampie.route.base.Render;
import cn.dreampie.route.http.HttpRequest;
import cn.dreampie.route.http.HttpResponse;
import cn.dreampie.util.HttpTyper;
import cn.dreampie.util.json.Jsoner;

/**
 * Created by ice on 14-12-29.
 *
 * @JsonerFiled(serialize=false)
 */
public class JsonRender extends Render {
  public void render(HttpRequest request, HttpResponse response, Object out) {
    if (out == null) {
      write(request, response, "");
    } else {
      String json = Jsoner.toJSONString(out);
      response.setContentType(HttpTyper.ContentType.JSON.toString());
      write(request, response, json);
    }
  }
}
