package cn.dreampie.route.interceptor.security;

import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.route.core.RouteInvocation;
import cn.dreampie.route.interceptor.Interceptor;
import cn.dreampie.security.*;

/**
 * Created by wangrenhui on 15/1/2.
 */
public class SecurityInterceptor implements Interceptor {

  private final SessionBuilder sessionBuilder;

  public SecurityInterceptor(AuthenticateService authenticateService) {
    this(500, 7, authenticateService, null);
  }

  public SecurityInterceptor(AuthenticateService authenticateService, PasswordService passwordService) {
    this(500, 7, authenticateService, passwordService);
  }

  public SecurityInterceptor(int limit, int rememberDay, AuthenticateService authenticateService, PasswordService passwordService) {
    if (passwordService != null)
      this.sessionBuilder = new SessionBuilder(limit, rememberDay, authenticateService, passwordService);
    else
      this.sessionBuilder = new SessionBuilder(limit, rememberDay, authenticateService);
  }

  public Object intercept(RouteInvocation ri) {
    HttpRequest request = ri.getRouteMatch().getRequest();
    HttpResponse response = ri.getRouteMatch().getResponse();

    //从cookie 构建session
    Session session = sessionBuilder.in(request);
    //检测权限
    Subject.check(request.getHttpMethod(), request.getRestPath());
    Object result = ri.invoke();
    //把session  写入cookie
    sessionBuilder.out(session, response);
    return result;
  }

}
