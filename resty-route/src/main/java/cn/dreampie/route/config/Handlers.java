package cn.dreampie.route.config;


import cn.dreampie.route.handler.Handler;

import java.util.ArrayList;
import java.util.List;

/**
 * Handlers.
 */
final public class Handlers {

  private final List<Handler> handlerList = new ArrayList<Handler>();

  public Handlers add(Handler handler) {
    if (handler != null)
      handlerList.add(handler);
    return this;
  }

  public List<Handler> getHandlerList() {
    return handlerList;
  }
}
