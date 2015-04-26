package cn.dreampie.route.render;

import cn.dreampie.common.Render;
import cn.dreampie.common.http.result.ImageResult;

import java.awt.image.RenderedImage;
import java.io.File;
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
    put("file", new FileRender());
    put("image", new ImageRender());
  }};

  private final static Map<Class<?>, Render> resultRenderMap = new HashMap<Class<?>, Render>() {{
    put(File.class, new FileRender());
    put(ImageResult.class, new ImageRender());
    put(RenderedImage.class, new ImageRender());
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

  public static void add(Class<?> resultType, Render render) {
    resultRenderMap.put(resultType, render);
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

  public static Render get(Class<?> resultType) {
    return resultRenderMap.get(resultType);
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
