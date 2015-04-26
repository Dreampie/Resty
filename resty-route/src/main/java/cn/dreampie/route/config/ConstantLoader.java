package cn.dreampie.route.config;

import cn.dreampie.common.Render;
import cn.dreampie.route.render.RenderFactory;

/**
 * The constant for Restj runtime.
 */
final public class ConstantLoader {
  //render
  public void addRender(String extension, Class<? extends Render> renderType) {
    RenderFactory.add(extension, renderType);
  }

  public void addRender(String extension, Class<? extends Render> renderType, boolean isDefault) {
    RenderFactory.add(extension, renderType, isDefault);
  }

  public void addRender(Class<?> resultType, Class<? extends Render> renderType, Class<?>... extResultTypes) {
    RenderFactory.add(resultType, renderType, extResultTypes);
  }

}







