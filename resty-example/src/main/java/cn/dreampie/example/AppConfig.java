package cn.dreampie.example;

import cn.dreampie.route.config.*;
import cn.dreampie.route.render.JsonRender;

/**
 * Created by ice on 14-12-29.
 */
public class AppConfig extends Config {
  public void configConstant(ConstantLoader constantLoader) {
//    constantLoader.addRender("json", new JsonRender());
  }

  public void configResource(ResourceLoader resourceLoader) {

  }

  public void configPlugin(PluginLoader pluginLoader) {

  }

  public void configInterceptor(InterceptorLoader interceptorLoader) {

  }

  public void configHandler(HandlerLoader handlerLoader) {

  }
}
