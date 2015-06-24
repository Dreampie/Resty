package cn.dreampie.route;


import cn.dreampie.common.Plugin;
import cn.dreampie.route.config.*;
import cn.dreampie.route.exception.PluginException;

import java.util.List;

public class ConfigIniter {

  private static final Constants CONSTANTS = new Constants();
  private static final Resources RESOURCES = new Resources();
  private static final Plugins PLUGINS = new Plugins();
  private static final Interceptors INTERCEPTORS = new Interceptors();
  private static final Handlers HANDLERS = new Handlers();

  public ConfigIniter(Config config) {
    config.configConstant(CONSTANTS);
    config.configPlugin(PLUGINS);
    startPlugins();//must start plugin before init other
    config.configResource(RESOURCES);
    buildResource();//scan resource class
    config.configInterceptor(INTERCEPTORS);
    config.configHandler(HANDLERS);
  }

  public Constants getConstants() {
    return CONSTANTS;
  }

  public Resources getResources() {
    return RESOURCES;
  }

  public void buildResource() {
    RESOURCES.build();
  }

  public Plugins getPlugins() {
    return PLUGINS;
  }

  public Interceptors getInterceptors() {
    return INTERCEPTORS;
  }

  public Handlers getHandlers() {
    return HANDLERS;
  }


  public void startPlugins() {
    List<Plugin> plugins = PLUGINS.getPlugins();
    if (plugins != null) {
      for (Plugin plugin : plugins) {
        if (!plugin.start()) {
          throw new PluginException("Plugin start error: " + plugin.getClass().getName());
        }
      }
    }
  }

  public void stopPlugins() {
    List<Plugin> plugins = PLUGINS.getPlugins();
    if (plugins != null) {
      for (Plugin plugin : plugins) {
        if (!plugin.stop()) {
          throw new PluginException("Plugin stop error: " + plugin.getClass().getName());
        }
      }
    }
  }

}
