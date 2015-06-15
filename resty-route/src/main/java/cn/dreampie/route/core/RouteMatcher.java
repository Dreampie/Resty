package cn.dreampie.route.core;

import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;

import static cn.dreampie.common.util.Checker.checkNotNull;

/**
 * Created by ice on 14-12-19.
 */
public class RouteMatcher {

  private final String pattern;
  private final String path;
  private final String extension;
  private final Params params;
  private final HttpRequest request;
  private final HttpResponse response;

  public RouteMatcher(String pattern, String path, String extension,
                      Params params, HttpRequest request, HttpResponse response) {

    this.pattern = checkNotNull(pattern);
    this.path = checkNotNull(path);
    this.params = checkNotNull(params);
    this.extension = checkNotNull(extension);
    this.request = request;
    this.response = response;
  }

  public String getPath() {
    return path;
  }

  public Params getParams() {
    return params;
  }

  public String getExtension() {
    return extension;
  }

  public HttpRequest getRequest() {
    return request;
  }

  public HttpResponse getResponse() {
    return response;
  }


  public String toString() {
    return "RouteMatcher{" +
        "pattern='" + pattern + '\'' +
        ", path='" + path + '\'' +
        '}';
  }
}
