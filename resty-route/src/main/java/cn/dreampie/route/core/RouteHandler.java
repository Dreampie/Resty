package cn.dreampie.route.core;

import cn.dreampie.common.http.HttpMethod;
import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.common.http.exception.WebException;
import cn.dreampie.common.http.result.HttpStatus;
import cn.dreampie.log.Logger;
import cn.dreampie.route.handler.Handler;

import java.util.Map;
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
    RouteInvocation routeInvocation = null;
    //请求的rest路径
    String restPath = request.getRestPath();
    Map<String, Map<String, Set<Route>>> routesMap = routeBuilder.getRoutesMap();
    Set<Route> routesSet;
    String httpMethod = request.getHttpMethod();
    boolean supportMethod = HttpMethod.support(httpMethod);
    //httpmethod区分
    if (supportMethod) {
      if (routesMap.containsKey(httpMethod)) {
        Set<Map.Entry<String, Set<Route>>> routesEntrySet = routesMap.get(httpMethod).entrySet();
        //url区分
        for (Map.Entry<String, Set<Route>> routesEntry : routesEntrySet) {
          if (restPath.startsWith(routesEntry.getKey())) {
            routesSet = routesEntry.getValue();
            for (Route r : routesSet) {
              routeMatch = r.match(request, response);
              if (routeMatch != null) {
                route = r;
                break;
              }
            }
            if (routeMatch != null) {
              break;
            }
          }
        }
      }

      if (routeMatch != null) {
        routeInvocation = new RouteInvocation(route, routeMatch);
      }
    }
    isHandled[0] = true;
    //route
    if (routeInvocation != null) {
      routeInvocation.invoke();
    } else {
      if (!restPath.equals("/") && supportMethod) {
        // no route matched
        throw new WebException(HttpStatus.SERVICE_UNAVAILABLE, "API is unavailable,check request body.");
      } else {
        isHandled[0] = false;
      }
    }
  }
}





