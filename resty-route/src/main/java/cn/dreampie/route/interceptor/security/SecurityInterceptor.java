package cn.dreampie.route.interceptor.security;

import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.route.core.RouteInvocation;
import cn.dreampie.route.interceptor.Interceptor;
import cn.dreampie.security.AuthenticateService;
import cn.dreampie.security.Session;
import cn.dreampie.security.SessionBuilder;
import cn.dreampie.security.Subject;

/**
 * Created by wangrenhui on 15/1/2.
 */
public class SecurityInterceptor implements Interceptor {

  private static final int expires = 30 * 60 * 1000;
  private static final int rememberDay = 7;
  private static final int limit = 100;
  private final SessionBuilder sessionBuilder;

  public SecurityInterceptor(AuthenticateService authenticateService) {
    this(expires, limit, rememberDay, authenticateService);
  }

  public SecurityInterceptor(int limit, AuthenticateService authenticateService) {
    this(expires, limit, rememberDay, authenticateService);
  }

  public SecurityInterceptor(int expires, int rememberDay, AuthenticateService authenticateService) {
    this(expires, limit, rememberDay, authenticateService);
  }

  public SecurityInterceptor(int expires, int limit, int rememberDay, AuthenticateService authenticateService) {
    if (limit < 1) {
      throw new IllegalArgumentException("Session limit must greater than 1.");
    }
    if (rememberDay > 30) {
      throw new IllegalArgumentException("RememberMe must less than 30 days.");
    }
    this.sessionBuilder = new SessionBuilder(expires, limit, rememberDay, authenticateService);
  }

  public void intercept(RouteInvocation ri) {
    HttpRequest request = ri.getRouteMatch().getRequest();
    HttpResponse response = ri.getRouteMatch().getResponse();

    //从cookie 构建session
    Session oldSession = sessionBuilder.in(request);
    //检测权限
    Subject.check(request.getHttpMethod(), request.getRestPath());
    //执行resource
    ri.invoke();
    //把session  写入cookie
    sessionBuilder.out(oldSession, response);
  }

}
