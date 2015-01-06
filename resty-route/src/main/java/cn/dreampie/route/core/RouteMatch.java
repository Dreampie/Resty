package cn.dreampie.route.core;

import cn.dreampie.common.Render;
import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.route.render.RenderFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.dreampie.common.util.Checker.checkNotNull;

/**
 * Created by ice on 14-12-19.
 */
public class RouteMatch {
  private final String pattern;
  private final String path;
  private final Render render;
  private final Map<String, String> pathParams;
  private final Map<String, List<String>> otherParams;
  private final HttpRequest request;
  private final HttpResponse response;

  public RouteMatch(String path, HttpRequest request, HttpResponse response) {
    this(path, path, new HashMap<String, String>(), request, response);
  }

  public RouteMatch(String pattern, String path, HttpRequest request, HttpResponse response) {
    this(pattern, path, new HashMap<String, String>(), request, response);
  }

  public RouteMatch(String pattern, String path, Map<String, String> pathParams, HttpRequest request, HttpResponse response) {
    this(pattern, path, "", pathParams, new HashMap<String, List<String>>(), request, response);
  }

  public RouteMatch(String pattern, String path, String extension, Map<String, String> pathParams, HttpRequest request, HttpResponse response) {
    this(pattern, path, "", pathParams, new HashMap<String, List<String>>(), request, response);
  }


  public RouteMatch(String pattern, String path, String extension,
                    Map<String, String> pathParams,
                    Map<String, List<String>> otherParams, HttpRequest request, HttpResponse response) {
    this(pattern, path, RenderFactory.get(extension), pathParams, otherParams, request, response);
  }

  public RouteMatch(String pattern, String path, Render render,
                    Map<String, String> pathParams,
                    Map<String, List<String>> otherParams, HttpRequest request, HttpResponse response) {
    this.pattern = checkNotNull(pattern);
    this.path = checkNotNull(path);
    this.pathParams = checkNotNull(pathParams);
    this.otherParams = checkNotNull(otherParams);
    this.render = render;
    this.request = request;
    this.response = response;
  }

  public String getPath() {
    return path;
  }

  public String getPathParam(String paramName) {
    String v = pathParams.get(paramName);
    checkNotNull(v, "path parameter %s was not found", paramName);
    return v;
  }

  public Map<String, String> getPathParams() {
    return pathParams;
  }

  public Map<String, List<String>> getOtherParams() {
    return otherParams;
  }

  public List<String> getOtherParam(String name) {
    return otherParams.get(name);
  }

  public Render getRender() {
    return render;
  }

  public HttpRequest getRequest() {
    return request;
  }

  public HttpResponse getResponse() {
    return response;
  }

  public String toString() {
    return "RouteMatch{" +
        "pattern='" + pattern + '\'' +
        ", path='" + path + '\'' +
        ", pathParams=" + pathParams +
        ", otherParams=" + otherParams +
        '}';
  }
}
