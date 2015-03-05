package cn.dreampie.route.core;

import cn.dreampie.common.Constant;
import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.common.http.HttpStatus;
import cn.dreampie.common.http.WebResult;
import cn.dreampie.common.http.exception.WebException;
import cn.dreampie.route.handler.Handler;
import cn.dreampie.route.render.FileRender;

import java.io.File;

/**
 * ActionHandler
 */
public final class RouteHandler extends Handler {

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

    for (Route r : routeBuilder.getRoutes()) {
      routeMatch = r.match(request, response);
      if (routeMatch != null) {
        route = r;
        break;
      }
    }

    isHandled[0] = true;
    if (routeMatch != null) {
      Object invokeResult = new RouteInvocation(route, routeMatch).invoke();
      Object result;
      //通过特定的webresult返回并携带状态码
      if (invokeResult instanceof WebResult) {
        WebResult webResult = (WebResult) invokeResult;
        response.setStatus(webResult.getStatus());
        result = webResult.getResult();
      } else {
        result = invokeResult;
      }
      //file render
      if (result instanceof File) {
        new FileRender().render(request, response, result);
      } else {
        routeMatch.getRender().render(request, response, result);
      }
    } else {
      if (request.getRestPath().equals(Constant.apiPrefix))
        // no route matched
        throw new WebException(HttpStatus.NOT_FOUND, "No rest route found.");
      else
        isHandled[0] = false;
    }
  }
}





