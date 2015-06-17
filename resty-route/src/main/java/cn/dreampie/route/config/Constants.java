package cn.dreampie.route.config;

import cn.dreampie.common.Render;
import cn.dreampie.common.http.result.HttpStatus;
import cn.dreampie.route.holder.ExceptionHolder;
import cn.dreampie.route.render.RenderFactory;

/**
 * The constant for Resty runtime.
 */
final public class Constants {

  public void setDefaultForward(String url) {
    ExceptionHolder.setDefaultForward(url);
  }

  public void setDefaultRedirect(String url) {
    ExceptionHolder.setDefaultRedirect(url);
  }

  public void addFoward(HttpStatus status, String url) {
    ExceptionHolder.addFoward(status, url);
  }

  public void addRedirect(HttpStatus status, String url) {
    ExceptionHolder.addRedirect(status, url);
  }

  //render
  public void addRender(String extension, Render render) {
    RenderFactory.add(extension, render);
  }

  public void addDefaultRender(String extension, Render render) {
    RenderFactory.addDefault(extension, render);
  }

}







