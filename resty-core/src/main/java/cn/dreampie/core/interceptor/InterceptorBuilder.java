package cn.dreampie.core.interceptor;

import cn.dreampie.core.interceptor.annotation.ClearInterceptors;
import cn.dreampie.core.interceptor.annotation.Interceptors;
import cn.dreampie.core.route.base.Resource;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * InterceptorBuilder
 */
public class InterceptorBuilder {
  private static final Interceptor[] NULL_INTERCEPTOR_ARRAY = new Interceptor[0];

  public void addToInterceptorsMap(Interceptor[] defaultInters) {
    for (Interceptor inter : defaultInters)
      intersMap.put((Class<Interceptor>) inter.getClass(), inter);
  }

  /**
   * Build interceptors of Resource
   */
  public Interceptor[] buildResourceInterceptors(Class<? extends Resource> resourceClass) {
    Interceptors before = resourceClass.getAnnotation(Interceptors.class);
    return before != null ? createInterceptors(before) : NULL_INTERCEPTOR_ARRAY;
  }

  /**
   * Build interceptors of Method
   */
  public Interceptor[] buildMethodInterceptors(Method method) {
    Interceptors before = method.getAnnotation(Interceptors.class);
    return before != null ? createInterceptors(before) : NULL_INTERCEPTOR_ARRAY;
  }

  /**
   * Build interceptors of Action
   */
  public Interceptor[] buildRouteInterceptors(Interceptor[] defaultInters, Interceptor[] resourceInters, Class<? extends Resource> resourceClass, Interceptor[] methodInters, Method method) {

    int size = defaultInters.length + resourceInters.length + methodInters.length;
    Interceptor[] allInters = (size == 0 ? NULL_INTERCEPTOR_ARRAY : new Interceptor[size]);

    int index = 0;
    for (int i = 0; i < defaultInters.length; i++) {
      allInters[index++] = defaultInters[i];
    }
    for (int i = 0; i < resourceInters.length; i++) {
      allInters[index++] = resourceInters[i];
    }
    for (int i = 0; i < methodInters.length; i++) {
      allInters[index++] = methodInters[i];
    }
    //去除clean的aop

    Class<? extends Interceptor>[] resourceClears = getResourceClears(resourceClass);
    Class<? extends Interceptor>[] methodClears = getMethodClears(method);
    for (int i = 0; i < allInters.length; i++) {
      i = clearInterceptor(allInters, resourceClears, i);
      i = clearInterceptor(allInters, methodClears, i);
    }

    return allInters;
  }

  private int clearInterceptor(Interceptor[] allInters, Class<? extends Interceptor>[] clears, int i) {
    if (clears != null && clears.length > 0) {
      for (Class<? extends Interceptor> ic : clears) {
        if (ic == allInters[i].getClass()) {
          allInters[i] = allInters[i + 1];
          i--;
        }
      }
    }
    return i;
  }

  private Class<? extends Interceptor>[] getMethodClears(Method method) {
    ClearInterceptors clearInterceptor = method.getAnnotation(ClearInterceptors.class);
    return clearInterceptor != null ? clearInterceptor.value() : null;
  }

  private Class<? extends Interceptor>[] getResourceClears(Class<? extends Resource> resourceClass) {
    ClearInterceptors clearInterceptor = resourceClass.getAnnotation(ClearInterceptors.class);
    return clearInterceptor != null ? clearInterceptor.value() : null;
  }

  private Map<Class<Interceptor>, Interceptor> intersMap = new HashMap<Class<Interceptor>, Interceptor>();

  /**
   * Create interceptors with Annotation of Aspect. Singleton version.
   */
  private Interceptor[] createInterceptors(Interceptors aroundAnnotation) {
    Interceptor[] result = null;
    Class<Interceptor>[] interceptorClasses = (Class<Interceptor>[]) aroundAnnotation.value();
    if (interceptorClasses != null && interceptorClasses.length > 0) {
      result = new Interceptor[interceptorClasses.length];
      for (int i = 0; i < result.length; i++) {
        result[i] = intersMap.get(interceptorClasses[i]);
        if (result[i] != null)
          continue;

        try {
          result[i] = interceptorClasses[i].newInstance();
          intersMap.put(interceptorClasses[i], result[i]);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }
    return result;
  }
}




