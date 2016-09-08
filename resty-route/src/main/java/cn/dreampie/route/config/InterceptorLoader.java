package cn.dreampie.route.config;


import cn.dreampie.route.interceptor.Interceptor;

import java.util.ArrayList;
import java.util.List;

/**
 * The interceptors applied to all actions.
 */
final public class InterceptorLoader extends Loader {

  private final List<Interceptor> interceptors = new ArrayList<Interceptor>();

  public InterceptorLoader add(Interceptor interceptor) {
    if (interceptor != null) {
      if (!this.interceptors.contains(interceptor)) {
        this.interceptors.add(interceptor);
      }
    }
    return this;
  }

  public Interceptor[] getInterceptorArray() {
    Interceptor[] result = interceptors.toArray(new Interceptor[interceptors.size()]);
    return result;
  }

  public void clear() {
    interceptors.clear();
  }
}
