package cn.dreampie.core.render;

import cn.dreampie.core.base.Render;
import cn.dreampie.core.http.HttpRequest;
import cn.dreampie.core.http.HttpResponse;
import cn.dreampie.util.HttpTyper;
import com.alibaba.fastjson.JSON;

/**
 * Created by ice on 14-12-29.
 *
 * @JSONFiled(serialize=false)
 */
public class JsonRender extends Render {
  public void render(HttpRequest request, HttpResponse response, Object out) {
    String json = JSON.toJSONString(out);
    response.setContentType(HttpTyper.ContentType.JSON.toString());
    write(request, response, json);
  }
}
