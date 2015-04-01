package cn.dreampie.route.config;


import cn.dreampie.common.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Plugins.
 */
final public class PluginLoader {

  private final List<Plugin> plugins = new ArrayList<Plugin>();

  public PluginLoader add(Plugin plugin) {
    if (plugin != null)
      this.plugins.add(plugin);
    return this;
  }

  public List<Plugin> getPlugins() {
    return plugins;
  }
}
