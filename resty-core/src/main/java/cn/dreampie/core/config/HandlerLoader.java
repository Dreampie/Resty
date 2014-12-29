package cn.dreampie.core.config;


import cn.dreampie.core.handler.Handler;

import java.util.ArrayList;
import java.util.List;

/**
 * Handlers.
 */
final public class HandlerLoader {

  private final List<Handler> handlerList = new ArrayList<Handler>();

  public HandlerLoader add(Handler handler) {
    if (handler != null)
      handlerList.add(handler);
    return this;
  }

  public List<Handler> getHandlerList() {
    return handlerList;
  }
}
