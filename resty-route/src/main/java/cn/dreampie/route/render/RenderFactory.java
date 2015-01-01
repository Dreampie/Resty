package cn.dreampie.route.render;

import cn.dreampie.route.base.Render;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ice on 14-12-29.
 */
public class RenderFactory {

  private static String defaultExtension = "json";

  private static Map<String, Render> renderMap = new HashMap<String, Render>() {{
    put("json", new JsonRender());
    put("text", new TextRender());
  }};

  public static void add(String extension, Render render) {
    renderMap.put(extension, render);
  }

  public static void add(String extension, Render render, boolean isDefault) {
    renderMap.put(extension, render);
    if (isDefault) defaultExtension = extension;
  }

  public static Render getRender(String extension) {
    Render render = renderMap.get(extension);
    if (render == null) {
      render = renderMap.get(defaultExtension);
    }
    return render;
  }

  public static Render getDefaultRender() {
    return renderMap.get(defaultExtension);
  }
}
