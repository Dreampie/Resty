package cn.dreampie.route.handler.resource;

import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.common.util.pattern.AntPathMatcher;
import cn.dreampie.route.handler.Handler;

/**
 * Created by wangrenhui on 2014/6/24.
 */
public class SkipHandler extends Handler {


  /**
   * 跳过的url
   */
  private String[] skipUrls;


  public SkipHandler(String... skipUrls) {
    this.skipUrls = skipUrls;
  }

  public void handle(HttpRequest request, HttpResponse response, boolean[] isHandled) {
    if (checkSkip(request.getRestPath())) {
      return;
    }
    nextHandler.handle(request, response, isHandled);
  }


  public boolean checkSkip(String skipUrl) {
    if (skipUrls != null && skipUrls.length > 0) {
      for (String url : skipUrls) {
        if (AntPathMatcher.instance().match(url, skipUrl)) {
          return true;
        }
      }
    }
    return false;
  }
}
