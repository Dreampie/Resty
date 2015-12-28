package cn.dreampie.route.render;

import cn.dreampie.common.Render;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ice on 14-12-29.
 */
public class RenderFactory {

  public final static String JSON = "json";
  public final static String TEXT = "text";
  public final static String FILE = "file";
  public final static String IMAGE = "image";
  private static String defaultExtension = JSON;
  private static Map<String, Render> renderMap = new HashMap<String, Render>() {{
    put(JSON, new JsonRender());
    put(TEXT, new TextRender());
    put(FILE, new FileRender());
    put(IMAGE, new ImageRender());
  }};


  public static void add(String extension, Render render) {
    if (!(render instanceof FileRender) && !(render instanceof ImageRender)) {
      renderMap.put(extension, render);
    }
  }

  public static void addDefault(String extension, Render render) {
    if (!(render instanceof FileRender) && !(render instanceof ImageRender)) {
      renderMap.put(extension, render);
      defaultExtension = extension;
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
      return renderMap.get(defaultExtension);
    } else {
      return render;
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


  public static Render getJsonRender() {
    return renderMap.get(JSON);
  }

  public static Render getTextRender() {
    return renderMap.get(TEXT);
  }

  public static Render getFileRender() {
    return renderMap.get(FILE);
  }

  public static Render getImageRender() {
    return renderMap.get(IMAGE);
  }
}
