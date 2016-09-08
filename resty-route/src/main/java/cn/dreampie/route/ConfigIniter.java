package cn.dreampie.route;


import cn.dreampie.common.Plugin;
import cn.dreampie.route.config.*;
import cn.dreampie.route.exception.PluginException;

import java.util.List;

public class ConfigIniter {

  private static final ConstantLoader constantLoader = new ConstantLoader();
  private static final ResourceLoader resourceLoader = new ResourceLoader();
  private static final PluginLoader pluginLoader = new PluginLoader();
  private static final InterceptorLoader interceptorLoader = new InterceptorLoader();
  private static final HandlerLoader handlerLoader = new HandlerLoader();

  public ConfigIniter(Config config) {
    config.configConstant(constantLoader);
    config.configPlugin(pluginLoader);
    startPlugins();//must start plugin before init other
    config.configResource(resourceLoader);
    buildRrsource();//scan  resource class
    config.configInterceptor(interceptorLoader);
    config.configHandler(handlerLoader);
  }

  public ConstantLoader getConstantLoader() {
    return constantLoader;
  }

  public ResourceLoader getResourceLoader() {
    return resourceLoader;
  }

  public PluginLoader getPluginLoader() {
    return pluginLoader;
  }

  public InterceptorLoader getInterceptorLoader() {
    return interceptorLoader;
  }

  public HandlerLoader getHandlerLoader() {
    return handlerLoader;
  }

  public void buildRrsource() {
    resourceLoader.build();
  }

  public void startPlugins() {
    List<Plugin> plugins = pluginLoader.getPlugins();
    if (plugins != null) {
      for (Plugin plugin : plugins) {
        if (!plugin.start()) {
          throw new PluginException("Plugin start error: " + plugin.getClass().getName());
        }
      }
    }
  }

  public void stopPlugins() {
    List<Plugin> plugins = pluginLoader.getPlugins();
    if (plugins != null) {
      for (Plugin plugin : plugins) {
        if (!plugin.stop()) {
          throw new PluginException("Plugin stop error: " + plugin.getClass().getName());
        }
      }
    }
  }

  public void clear() {
    constantLoader.clear();
    resourceLoader.clear();
    pluginLoader.clear();
    interceptorLoader.clear();
    handlerLoader.clear();
  }

}
