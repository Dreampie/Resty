package cn.dreampie.route.core;

import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.common.http.HttpStatus;
import cn.dreampie.common.http.exception.WebException;
import cn.dreampie.log.Logger;
import cn.dreampie.route.handler.Handler;

/**
 * ActionHandler
 */
public final class RouteHandler extends Handler {

  private final RouteBuilder resourceBuilder;
  private static final Logger logger = Logger.getLogger(RouteHandler.class);

  public RouteHandler(RouteBuilder resourceBuilder) {
    this.resourceBuilder = resourceBuilder;
  }

  /**
   * handle
   */
  public final void handle(HttpRequest request, HttpResponse response, boolean[] isHandled) {

    RouteMatch routeMatch = null;
    Route route = null;

    for (Route r : resourceBuilder.getRoutes()) {
      routeMatch = r.match(request, response);
      if (routeMatch != null) {
        route = r;
        break;
      }
    }

    isHandled[0] = true;
    if (routeMatch != null) {
      response.setStatus(HttpStatus.OK);
      routeMatch.getRender().render(request, response, new RouteInvocation(route, routeMatch).invoke());
    } else {
      if (!request.getRestPath().equals("/"))
        // no route matched
        throw new WebException(HttpStatus.NOT_FOUND, "No rest route found.");
      else
        isHandled[0] = false;
    }
  }
}





