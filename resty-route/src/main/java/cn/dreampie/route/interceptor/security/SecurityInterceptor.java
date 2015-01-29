package cn.dreampie.route.interceptor.security;

import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.route.core.RouteInvocation;
import cn.dreampie.route.exception.InitException;
import cn.dreampie.route.interceptor.Interceptor;
import cn.dreampie.security.*;

/**
 * Created by wangrenhui on 15/1/2.
 */
public class SecurityInterceptor implements Interceptor {

  private final SessionBuilder sessionBuilder;
  private static final int expires = 20 * 60 * 1000;
  private static final int rememberDay = 7;
  private static final int limit = 100;

  public SecurityInterceptor(AuthenticateService authenticateService) {
    this(expires, limit, rememberDay, authenticateService);
  }

  public SecurityInterceptor(int limit, AuthenticateService authenticateService) {
    this(expires, limit, rememberDay, authenticateService);
  }

  public SecurityInterceptor(AuthenticateService authenticateService, PasswordService passwordService) {
    this(expires, limit, rememberDay, authenticateService, passwordService);
  }

  public SecurityInterceptor(int expires, int rememberDay, AuthenticateService authenticateService) {
    this(expires, limit, rememberDay, authenticateService, null);
  }

  public SecurityInterceptor(int expires, int limit, int rememberDay, AuthenticateService authenticateService) {
    this(expires, limit, rememberDay, authenticateService, null);
  }

  public SecurityInterceptor(int expires, int limit, int rememberDay, AuthenticateService authenticateService, PasswordService passwordService) {
    if (rememberDay > 30)
      throw new InitException("RememberMe must less than 30 days.");
    if (passwordService != null)
      this.sessionBuilder = new SessionBuilder(expires, limit, rememberDay, authenticateService, passwordService);
    else
      this.sessionBuilder = new SessionBuilder(expires, limit, rememberDay, authenticateService);
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
    Session newSession = sessionBuilder.out(session, request, response);
    //保存session到cache
    sessionBuilder.buildSessionMetadata(request, newSession);

    return result;
  }

}
