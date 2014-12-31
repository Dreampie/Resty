package cn.dreampie.core;


import cn.dreampie.common.Plugin;
import cn.dreampie.core.config.*;
import cn.dreampie.log.Logger;
import cn.dreampie.log.LoggerFactory;

import java.util.List;

public class ConfigIniter {

  private static final ConstantLoader CONSTANT_LOADER = new ConstantLoader();
  private static final ResourceLoader RESOURCE_LOADER = new ResourceLoader();
  private static final PluginLoader PLUGIN_LOADER = new PluginLoader();
  private static final InterceptorLoader INTERCEPTOR_LOADER = new InterceptorLoader();
  private static final HandlerLoader HANDLER_LOADER = new HandlerLoader();
  private static final Logger logger = LoggerFactory.getLogger(ConfigIniter.class);

  // prevent new Config();
  private ConfigIniter() {
  }

  /*
   * Config order: constant, route, plugin, interceptor, handler
   */
  static void config(Config config) {
    config.configConstant(CONSTANT_LOADER);
    config.configResource(RESOURCE_LOADER);
    config.configPlugin(PLUGIN_LOADER);
    startPlugins();  // very important!!!
    config.configInterceptor(INTERCEPTOR_LOADER);
    config.configHandler(HANDLER_LOADER);
  }

  public static ConstantLoader getConstantLoader() {
    return CONSTANT_LOADER;
  }

  public static ResourceLoader getResourceLoader() {
    return RESOURCE_LOADER;
  }

  public static PluginLoader getPluginLoader() {
    return PLUGIN_LOADER;
  }

  public static InterceptorLoader getInterceptorLoader() {
    return INTERCEPTOR_LOADER;
  }

  public static HandlerLoader getHandlerLoader() {
    return HANDLER_LOADER;
  }

  private static void startPlugins() {
    List<Plugin> pluginList = PLUGIN_LOADER.getPluginList();
    if (pluginList == null)
      return;

    for (Plugin plugin : pluginList) {
      try {
        if (!plugin.start()) {
          logger.error("Plugin start error: " + plugin.getClass().getName());
        }
      } catch (Exception e) {
        logger.error("Plugin start error: " + plugin.getClass().getName() + ". \n" + e.getMessage(), e);
      }
    }
  }

}
