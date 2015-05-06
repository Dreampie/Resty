package cn.dreampie.route.core;


import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;

/**
 * Resource
 */
public abstract class Resource {

  private RouteMatch routeMatch;

  void setRouteMatch(RouteMatch routeMatch) {
    this.routeMatch = routeMatch;
  }

  public String getPath() {
    return routeMatch.getPath();
  }

  public Params getParams() {
    return routeMatch.getParams();
  }

  /**
   * Get param of any type.
   */
  public <T> T get(String name) {
    return (T) (getParams().get(name));
  }

  public HttpRequest getRequest() {
    return routeMatch.getRequest();
  }

  public HttpResponse getResponse() {
    return routeMatch.getResponse();
  }

}


