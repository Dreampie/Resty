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
    //请求的rest路径
    String restPath = request.getRestPath();
    Map<String, Map<String, Set<Route>>> routesMap = routeBuilder.getRoutesMap();
    Set<Route> routesSet;
    String httpMethod = request.getHttpMethod();
    //httpmethod区分
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

    isHandled[0] = true;
    if (routeMatch != null) {
      new RouteInvocation(route, routeMatch).invoke();
    } else {
      if (!restPath.equals("/")) {
        if (restPath.startsWith(Constant.apiPrefix)) {
          // no route matched
          throw new WebException(HttpStatus.NOT_FOUND, "No rest route found.");
        } else {
          try {
            response.forward(Constant.notFound);
          } catch (ServletException e) {
            logger.error("Request forward '" + Constant.notFound + "' error.", e);
          } catch (IOException e) {
            logger.error("Request forward '" + Constant.notFound + "' error.", e);
          }
        }
      } else {
        isHandled[0] = false;
      }
    }
  }
}





