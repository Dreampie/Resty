package cn.dreampie.route.interceptor.security;

import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.route.core.RouteInvocation;
import cn.dreampie.route.interceptor.Interceptor;
import cn.dreampie.security.AuthenticateService;
import cn.dreampie.security.Session;
import cn.dreampie.security.SessionBuilder;
import cn.dreampie.security.Subject;
import cn.dreampie.security.builder.BothSessionBuilder;

/**
 * Created by wangrenhui on 15/1/2.
 */
public class SecurityInterceptor implements Interceptor {

  private final SessionBuilder sessionBuilder;

  public SecurityInterceptor(AuthenticateService authenticateService) {
    this.sessionBuilder = new BothSessionBuilder(authenticateService);
  }

  public SecurityInterceptor(SessionBuilder sessionBuilder) {
    this.sessionBuilder = sessionBuilder;
  }

  public void intercept(RouteInvocation ri) {
    HttpRequest request = ri.getRouteMatch().getRequest();
    HttpResponse response = ri.getRouteMatch().getResponse();

    //从cookie/header 构建session
    Session oldSession = sessionBuilder.in(request,response);
    //检测权限
    Subject.check(request.getHttpMethod(), request.getRestPath());
    //执行resource
    ri.invoke();
    //把session  写入cookie/header
    sessionBuilder.out(oldSession, response);
  }

}
