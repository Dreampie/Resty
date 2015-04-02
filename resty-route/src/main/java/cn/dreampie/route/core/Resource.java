package cn.dreampie.route.core;


import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.common.http.UploadedFile;

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

  public String getOtherParamFisrt(String name) {
    List<String> value = routeMatch.getOtherParams().get(name);
    return value != null ? value.get(0) : null;
  }

  public Map<String, UploadedFile> getFileParams() {
    return routeMatch.getFileParams();
  }

  public UploadedFile getFileParamFirst() {
    return routeMatch.getFileParamFirst();
  }

  public UploadedFile getFileParam(String filename) {
    return routeMatch.getFileParam(filename);
  }

  public HttpRequest getRequest() {
    return routeMatch.getRequest();
  }

  public HttpResponse getResponse() {
    return routeMatch.getResponse();
  }

}


