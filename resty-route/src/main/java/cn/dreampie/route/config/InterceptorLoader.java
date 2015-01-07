package cn.dreampie.route.config;


import cn.dreampie.route.interceptor.Interceptor;

import java.util.ArrayList;
import java.util.List;

/**
 * The interceptors applied to all actions.
 */
final public class InterceptorLoader {

  private final List<Interceptor> interceptorList = new ArrayList<Interceptor>();

  public InterceptorLoader add(Interceptor interceptor) {
    if (interceptor != null)
      this.interceptorList.add(interceptor);
    return this;
  }

  public Interceptor[] getInterceptorArray() {
    Interceptor[] result = interceptorList.toArray(new Interceptor[interceptorList.size()]);
    return result;
  }
}
