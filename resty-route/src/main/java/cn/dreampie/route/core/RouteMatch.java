package cn.dreampie.route.core;

import cn.dreampie.route.base.Render;
import cn.dreampie.route.render.RenderFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.dreampie.util.Checker.checkNotNull;

/**
 * Created by ice on 14-12-19.
 */
public class RouteMatch {
  private final String pattern;
  private final String path;
  private final Render render;
  private final Map<String, String> pathParams;
  private final Map<String, List<String>> otherParams;

  public RouteMatch(String path) {
    this(path, path, new HashMap<String, String>());
  }

  public RouteMatch(String pattern, String path) {
    this(pattern, path, new HashMap<String, String>());
  }

  public RouteMatch(String pattern, String path, Map<String, String> pathParams) {
    this(pattern, path, "", pathParams, new HashMap<String, List<String>>());
  }

  public RouteMatch(String pattern, String path, String extension, Map<String, String> pathParams) {
    this(pattern, path, "", pathParams, new HashMap<String, List<String>>());
  }


  public RouteMatch(String pattern, String path, String extension,
                    Map<String, String> pathParams,
                    Map<String, List<String>> otherParams) {
    this(pattern, path, RenderFactory.getRender(extension), pathParams, otherParams);
  }

  public RouteMatch(String pattern, String path, Render render,
                    Map<String, String> pathParams,
                    Map<String, List<String>> otherParams) {
    this.pattern = checkNotNull(pattern);
    this.path = checkNotNull(path);
    this.pathParams = checkNotNull(pathParams);
    this.otherParams = checkNotNull(otherParams);
    this.render = render;
  }

  public String getPath() {
    return path;
  }

  public String getPathParam(String paramName) {
    String v = pathParams.get(paramName);
    if (v == null) {
      throw new IllegalStateException(
          String.format("path parameter %s was not found", paramName));
    }
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

  public String toString() {
    return "StdRestjHandlerMatch{" +
        "pattern='" + pattern + '\'' +
        ", path='" + path + '\'' +
        ", pathParams=" + pathParams +
        ", otherParams=" + otherParams +
        '}';
  }
}
