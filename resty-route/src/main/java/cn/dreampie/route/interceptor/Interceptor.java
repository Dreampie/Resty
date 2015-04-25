package cn.dreampie.route.interceptor;


import cn.dreampie.route.core.RouteInvocation;

/**
 * Interceptor.
 */
public interface Interceptor {
  public void intercept(RouteInvocation ri);
}
