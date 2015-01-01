package cn.dreampie.route.core.base;


import cn.dreampie.route.core.RouteMatch;

import java.util.List;
import java.util.Map;

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

  public String getPathParam(String paramName) {
    return routeMatch.getPathParam(paramName);
  }

  public Map<String, String> getPathParams() {
    return routeMatch.getPathParams();
  }

  public Map<String, List<String>> getOtherParams() {
    return routeMatch.getOtherParams();
  }

  public List<String> getOtherParam(String name) {
    return routeMatch.getOtherParams().get(name);
  }


}


