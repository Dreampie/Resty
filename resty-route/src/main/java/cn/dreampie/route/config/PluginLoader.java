package cn.dreampie.route.config;


import cn.dreampie.common.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Plugins.
 */
final public class PluginLoader {

  private final List<Plugin> pluginList = new ArrayList<Plugin>();

  public PluginLoader add(Plugin plugin) {
    if (plugin != null)
      this.pluginList.add(plugin);
    return this;
  }

  public List<Plugin> getPluginList() {
    return pluginList;
  }
}
