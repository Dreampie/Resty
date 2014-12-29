package cn.dreampie.core.config;

import cn.dreampie.core.Constant;
import cn.dreampie.core.base.Render;
import cn.dreampie.core.render.RenderFactory;

import java.util.Map;

/**
 * The constant for Restj runtime.
 */
final public class ConstantLoader {

  public void setDevMode(boolean devMode) {
    Constant.DEV_MODE = devMode;
  }

  public void setDefaultEncoding(String defaultEncoding) {
    Constant.ENCODING = defaultEncoding;
  }

  public static void add(String extension, Render render) {
    RenderFactory.add(extension, render);
  }

  public static void addAll(Map<String, Render> renders) {
    RenderFactory.addAll(renders);
  }


}







