package cn.dreampie.route.config;

import cn.dreampie.common.Constant;
import cn.dreampie.route.base.Render;
import cn.dreampie.route.render.RenderFactory;
import cn.dreampie.orm.cache.CacheManager;

/**
 * The constant for Restj runtime.
 */
final public class ConstantLoader {

  public void setDevMode(boolean devMode) {
    Constant.dev_mode = devMode;
  }

  public void setDefaultEncoding(String defaultEncoding) {
    Constant.encoding = defaultEncoding;
  }

  public void addRender(String extension, Render render) {
    RenderFactory.add(extension, render);
  }

  public void addRender(String extension, Render render, boolean isDefault) {
    RenderFactory.add(extension, render, isDefault);
  }

  public void setCacheManager(CacheManager cacheManager) {
    Constant.cacheManager = cacheManager;
  }

  public void setCacheManager(CacheManager cacheManager, boolean cacheEnable) {
    Constant.cacheManager = cacheManager;
    Constant.cache_enabled = cacheEnable;
  }

  public void setCacheEnable(boolean cacheEnable) {
    Constant.cache_enabled = cacheEnable;
  }
}







