package cn.dreampie.route.render;

import cn.dreampie.common.Render;

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
    if (!(render instanceof FileRender))
      renderMap.put(extension, render);
  }

  public static void add(String extension, Render render, boolean isDefault) {
    if (!(render instanceof FileRender)) {
      renderMap.put(extension, render);
      if (isDefault) defaultExtension = extension;
    }
  }

  /**
   * 返回新的render对象
   *
   * @param extension 扩展名
   * @return
   */
  public static Render get(String extension) {
    Render render = renderMap.get(extension);
    if (render == null) {
      return renderMap.get(defaultExtension).newInstance();
    } else {
      return render.newInstance();
    }
  }

  public static Render getByUrl(String url) {
    String extension = "";
    if (url.contains(".")) {
      extension = url.substring(url.lastIndexOf(".") + 1);
    }
    return get(extension);
  }

  public static boolean contains(String extension) {
    return renderMap.containsKey(extension);
  }

  public static Render getDefaultRender() {
    return get(defaultExtension);
  }
}
