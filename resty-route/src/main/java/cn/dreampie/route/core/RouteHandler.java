package cn.dreampie.route.core;

import cn.dreampie.common.Constant;
import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.common.http.exception.WebException;
import cn.dreampie.common.http.result.HttpStatus;
import cn.dreampie.log.Logger;
import cn.dreampie.route.handler.Handler;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Set;

/**
 * ActionHandler
 */
public final class RouteHandler extends Handler {
  private final static Logger logger = Logger.getLogger(RouteHandler.class);
  private final RouteBuilder routeBuilder;

  public RouteHandler(RouteBuilder routeBuilder) {
    this.routeBuilder = routeBuilder;
  }

  /**
   * handle
   */
  public final void handle(HttpRequest request, HttpResponse response, boolean[] isHandled) {

    RouteMatch routeMatch = null;
    Route route = null;

    Set<Route> routeSet = routeBuilder.getRoutes();
    for (Route r : routeSet) {
      routeMatch = r.match(request, response);
      if (routeMatch != null) {
        route = r;
        break;
      }
    }

    isHandled[0] = true;
    if (routeMatch != null) {
      new RouteInvocation(route, routeMatch).invoke();
    } else {
      String restPath = request.getRestPath();
      if (!restPath.equals("/")) {
        if (restPath.startsWith(Constant.apiPrefix)) {
          // no route matched
          throw new WebException(HttpStatus.NOT_FOUND, "No rest route found.");
        } else {
          try {
            response.forward("/");
          } catch (ServletException e) {
            logger.error("Request forward '/' error.", e);
          } catch (IOException e) {
            logger.error("Request forward '/' error.", e);
          }
        }
      } else {
        isHandled[0] = false;
      }
    }
  }
}





