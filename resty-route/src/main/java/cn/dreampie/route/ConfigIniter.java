package cn.dreampie.route;


import cn.dreampie.common.Plugin;
import cn.dreampie.route.config.*;
import cn.dreampie.route.exception.PluginException;

import java.util.List;

public class ConfigIniter {

  private static final ConstantLoader CONSTANT_LOADER = new ConstantLoader();
  private static final ResourceLoader RESOURCE_LOADER = new ResourceLoader();
  private static final PluginLoader PLUGIN_LOADER = new PluginLoader();
  private static final InterceptorLoader INTERCEPTOR_LOADER = new InterceptorLoader();
  private static final HandlerLoader HANDLER_LOADER = new HandlerLoader();

  public ConfigIniter(Config config) {
    config.configConstant(CONSTANT_LOADER);
    config.configPlugin(PLUGIN_LOADER);
    startPlugins();//must start plugin before init other
    config.configResource(RESOURCE_LOADER);
    buildRrsource();//scan  resource class
    config.configInterceptor(INTERCEPTOR_LOADER);
    config.configHandler(HANDLER_LOADER);
  }

  public ConstantLoader getConstantLoader() {
    return CONSTANT_LOADER;
  }

  public ResourceLoader getResourceLoader() {
    return RESOURCE_LOADER;
  }

  public PluginLoader getPluginLoader() {
    return PLUGIN_LOADER;
  }

  public InterceptorLoader getInterceptorLoader() {
    return INTERCEPTOR_LOADER;
  }

  public HandlerLoader getHandlerLoader() {
    return HANDLER_LOADER;
  }

  public void buildRrsource() {
    RESOURCE_LOADER.build();
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
