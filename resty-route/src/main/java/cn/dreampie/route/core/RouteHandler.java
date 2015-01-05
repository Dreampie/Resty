package cn.dreampie.route.core;

import cn.dreampie.log.Logger;
import cn.dreampie.route.handler.Handler;
import cn.dreampie.route.http.HttpRequest;
import cn.dreampie.route.http.HttpResponse;
import cn.dreampie.route.http.HttpStatus;
import cn.dreampie.route.http.exception.WebException;

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
    isHandled[0] = true;

    for (Route r : resourceBuilder.getRoutes()) {
      routeMatch = r.match(request);
      if (routeMatch != null) {
        route = r;
        break;
      }
    }

    if (routeMatch != null) {
      response.setStatus(HttpStatus.OK);
      routeMatch.getRender().render(request, response, new RouteInvocation(route, routeMatch).invoke());
    } else {
      // no route matched
      throw new WebException(HttpStatus.NOT_FOUND, "No rest route found.");
    }
  }
}





