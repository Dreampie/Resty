package cn.dreampie.route.config;

import cn.dreampie.common.Render;
import cn.dreampie.route.render.RenderFactory;

/**
 * The constant for Restj runtime.
 */
final public class ConstantLoader {
  //render
  public void addRender(String extension, Render render) {
    RenderFactory.add(extension, render);
  }

  public void addRender(String extension, Render render, boolean isDefault) {
    RenderFactory.add(extension, render, isDefault);
  }

}







