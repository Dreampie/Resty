package cn.dreampie.route.config;


import cn.dreampie.route.handler.Handler;

import java.util.ArrayList;
import java.util.List;

/**
 * Handlers.
 */
final public class HandlerLoader extends Loader {

  private final List<Handler> handlers = new ArrayList<Handler>();

  public HandlerLoader add(Handler handler) {
    if (handler != null) {
      if (!handlers.contains(handler)) {
        handlers.add(handler);
      }
    }
    return this;
  }

  public List<Handler> getHandlers() {
    return handlers;
  }

  public void clear() {
    handlers.clear();
  }
}
