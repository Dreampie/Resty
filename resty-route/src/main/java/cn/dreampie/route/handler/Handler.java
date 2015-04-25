package cn.dreampie.route.handler;

import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;

/**
 * Handler.
 * You can config Handler in JFinalConfig.configHandler() method,
 * Handler can do anything under the jfinal action.
 */
public abstract class Handler {

  protected Handler nextHandler;

  /**
   * Handle target
   *
   * @param request   HttpServletRequest of this http request
   * @param response  HttpServletRequest of this http request
   * @param isHandled RestjFilter will invoke doFilter() method if isHandled[0] == false,
   *                  it is usually to tell Filter should handle the static resource.
   */
  public abstract void handle(HttpRequest request, HttpResponse response, boolean[] isHandled);
}




