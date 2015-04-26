package cn.dreampie.route.render;

import cn.dreampie.common.Render;
import cn.dreampie.common.http.result.ImageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.RenderedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ice on 14-12-29.
 */
public class RenderFactory {

  private static final Logger LOG = LoggerFactory.getLogger(RenderFactory.class);

  private static String defaultExtension = "json";

  private static Map<String, Render> renderMap = new HashMap<String, Render>();
  private static Map<Class<?>, Render> resultRenderMap = new HashMap<Class<?>, Render>();

  private static Map<Class<? extends Render>, Render> renderCache = new HashMap<Class<? extends Render>, Render>();

  static {
    add("json", JsonRender.class);
    add("text", TextRender.class);
    add("file", FileRender.class);
    add("image", ImageRender.class);

    add(File.class, FileRender.class);
    add(ImageResult.class, ImageRender.class, RenderedImage.class);
  }


  public static void add(String extension, Class<? extends Render> renderType) {
    Render render = getRender(renderType);
    if (!(render instanceof FileRender))
      renderMap.put(extension, render);
  }

  public static void add(String extension, Class<? extends Render> renderType, boolean isDefault) {
    Render render = getRender(renderType);
    if (!(render instanceof FileRender)) {
      renderMap.put(extension, render);
      if (isDefault) defaultExtension = extension;
    }
  }

  public static void add(Class<?> resultType, Class<? extends Render> renderType, Class<?>... extResultTypes) {
    Render render = getRender(renderType);
    resultRenderMap.put(resultType, render);
    for (Class<?> cl : extResultTypes) {
      resultRenderMap.put(cl, render);
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

  private static Render getRender(Class<? extends Render> renderType) {
    synchronized (renderCache) {
      Render render = renderCache.get(renderType);
      if (render == null) {
        try {
          render = renderType.newInstance();
          renderCache.put(renderType, render);
        } catch (InstantiationException e) {
          LOG.error("", e);
        } catch (IllegalAccessException e) {
          LOG.error("", e);
        }
      }
      return render;
    }
  }

}
