package cn.dreampie.route.config;

import cn.dreampie.common.Render;
import cn.dreampie.common.http.result.HttpStatus;
import cn.dreampie.route.holder.ExceptionHolder;
import cn.dreampie.route.render.RenderFactory;

/**
 * The constant for Restj runtime.
 */
final public class ConstantLoader {

  public static void addExceptionHold(HttpStatus status, String url) {
    addExceptionHold(status, url, false);
  }

  public static void addExceptionHold(HttpStatus status, String url, boolean redirect) {
    ExceptionHolder.addExceptionHold(status, url, redirect);
  }

  //render
  public void addRender(String extension, Render render) {
    RenderFactory.add(extension, render);
  }

  public void addRender(String extension, Render render, boolean isDefault) {
    RenderFactory.add(extension, render, isDefault);
  }

}







