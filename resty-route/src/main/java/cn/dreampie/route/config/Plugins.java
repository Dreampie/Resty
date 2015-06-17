package cn.dreampie.route.config;


import cn.dreampie.common.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Plugins.
 */
final public class Plugins {

  private final List<Plugin> plugins = new ArrayList<Plugin>();

  public Plugins add(Plugin plugin) {
    if (plugin != null)
      this.plugins.add(plugin);
    return this;
  }

  public List<Plugin> getPlugins() {
    return plugins;
  }
}
