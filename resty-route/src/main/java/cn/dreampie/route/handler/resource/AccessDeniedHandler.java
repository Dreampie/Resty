package cn.dreampie.route.handler.resource;

import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.common.http.HttpStatus;
import cn.dreampie.common.http.exception.WebException;
import cn.dreampie.common.util.pattern.AntPathMatcher;
import cn.dreampie.route.handler.Handler;

/**
 * Created by wangrenhui on 2014/6/24.
 */
public class AccessDeniedHandler extends Handler {

  /**
   * 拒绝访问的url
   */
  private String[] accessDeniedUrls;


  public AccessDeniedHandler(String... accessDeniedUrls) {
    this.accessDeniedUrls = accessDeniedUrls;
  }

  public void handle(HttpRequest request, HttpResponse response, boolean[] isHandled) {
    if (checkView(request.getRestPath())) {
      isHandled[0] = true;
      throw new WebException(HttpStatus.FORBIDDEN);
    }
    nextHandler.handle(request, response, isHandled);
  }

  public boolean checkView(String viewUrl) {

    if (accessDeniedUrls != null && accessDeniedUrls.length > 0) {
      for (String url : accessDeniedUrls) {
        if (AntPathMatcher.instance().match(url, viewUrl)) {
          return true;
        }
      }
    }
    return false;
  }
}
