package cn.dreampie.core.render;

import cn.dreampie.core.base.Render;
import cn.dreampie.core.http.HttpRequest;
import cn.dreampie.core.http.HttpResponse;
import cn.dreampie.core.http.HttpStatus;
import cn.dreampie.kit.HttpTyper;
import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ice on 14-12-29.
 */
public class RenderFactory {
  private static Map<String, Render> renderMap = new HashMap<String, Render>() {{
    put("json", new JsonRender());
    put("text", new TextRender());
  }};

  public static void add(String extension, Render render) {
    renderMap.put(extension, render);
  }

  public static void addAll(Map<String, Render> renders) {
    renderMap.putAll(renders);
  }

  public static Render getRender(String extension) {
    Render render = renderMap.get(extension);
    if (render == null) {
      render = renderMap.get("json");
    }
    return render;
  }

  public static Render getDefaultRender() {
    return renderMap.get("json");
  }
}
