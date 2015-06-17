package cn.dreampie.route;


import cn.dreampie.common.Plugin;
import cn.dreampie.route.config.*;
import cn.dreampie.route.exception.PluginException;

import java.util.List;

public class ConfigIniter {

  private static final Constants CONSTANT_LOADER = new Constants();
  private static final Resources RESOURCE_LOADER = new Resources();
  private static final Plugins PLUGIN_LOADER = new Plugins();
  private static final Interceptors INTERCEPTOR_LOADER = new Interceptors();
  private static final Handlers HANDLER_LOADER = new Handlers();

  public ConfigIniter(Config config) {
    config.configConstant(CONSTANT_LOADER);
    config.configPlugin(PLUGIN_LOADER);
    startPlugins();//must start plugin before init other
    config.configResource(RESOURCE_LOADER);
    buildRrsource();//scan resource class
    config.configInterceptor(INTERCEPTOR_LOADER);
    config.configHandler(HANDLER_LOADER);
  }

  public Constants getConstantLoader() {
    return CONSTANT_LOADER;
  }

  public Resources getResourceLoader() {
    return RESOURCE_LOADER;
  }

  public void buildRrsource() {
    RESOURCE_LOADER.build();
  }

  public Plugins getPluginLoader() {
    return PLUGIN_LOADER;
  }

  public Interceptors getInterceptorLoader() {
    return INTERCEPTOR_LOADER;
  }

  public Handlers getHandlerLoader() {
    return HANDLER_LOADER;
  }


  public void startPlugins() {
    List<Plugin> plugins = PLUGIN_LOADER.getPlugins();
    if (plugins != null) {
      for (Plugin plugin : plugins) {
        if (!plugin.start()) {
          throw new PluginException("Plugin start error: " + plugin.getClass().getName());
        }
      }
    }
  }

  public void stopPlugins() {
    List<Plugin> plugins = PLUGIN_LOADER.getPlugins();
    if (plugins != null) {
      for (Plugin plugin : plugins) {
        if (!plugin.stop()) {
          throw new PluginException("Plugin stop error: " + plugin.getClass().getName());
        }
      }
    }
  }

}
