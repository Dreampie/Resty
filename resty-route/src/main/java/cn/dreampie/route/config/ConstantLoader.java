package cn.dreampie.route.config;

import cn.dreampie.common.Constant;
import cn.dreampie.common.Render;
import cn.dreampie.orm.cache.CacheManager;
import cn.dreampie.orm.cache.EHCacheManager;
import cn.dreampie.route.render.RenderFactory;

/**
 * The constant for Restj runtime.
 */
final public class ConstantLoader {

  public void setDevMode(boolean devMode) {
    Constant.dev_mode = devMode;
  }

  public void setShowRoute(boolean showRoute) {
    Constant.show_route = showRoute;
  }

  //encoding
  public void setDefaultEncoding(String defaultEncoding) {
    Constant.encoding = defaultEncoding;
  }

  //render
  public void addRender(String extension, Render render) {
    RenderFactory.add(extension, render);
  }

  public void addRender(String extension, Render render, boolean isDefault) {
    RenderFactory.add(extension, render, isDefault);
  }


  //cache
  public void setCacheManager(CacheManager cacheManager) {
    setCacheManager(cacheManager, true);
  }

  public void setCacheManager(CacheManager cacheManager, boolean cacheEnable) {
    Constant.cacheManager = cacheManager;
    Constant.cache_enabled = cacheEnable;
  }

  public void setCacheEnable(boolean cacheEnable) {
    setCacheManager(new EHCacheManager(), cacheEnable);
  }

  //upload
  public void setUploadDirectory(String uploadDirectory, int uploadMaxSize) {
    Constant.uploadDirectory = uploadDirectory;
    Constant.uploadMaxSize = uploadMaxSize;
  }

  public void setUploadDirectory(String uploadDirectory) {
    Constant.uploadDirectory = uploadDirectory;
  }

  public void setUploadMaxSize(int uploadMaxSize) {
    Constant.uploadMaxSize = uploadMaxSize;
  }

  public void setUploadDenieds(String... uploadDenieds) {
    Constant.uploadDenieds = uploadDenieds;
  }

}







