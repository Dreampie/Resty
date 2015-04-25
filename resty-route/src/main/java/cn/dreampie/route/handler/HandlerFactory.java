package cn.dreampie.route.handler;

import java.util.List;

/**
 * HandlerFactory.
 */
public class HandlerFactory {

  private HandlerFactory() {

  }

  /**
   * Build handler chain
   */
  public static Handler getHandler(List<Handler> handlerList, Handler actionHandler) {
    Handler result = actionHandler;

    for (int i = handlerList.size() - 1; i >= 0; i--) {
      Handler temp = handlerList.get(i);
      temp.nextHandler = result;
      result = temp;
    }

    return result;
  }
}




