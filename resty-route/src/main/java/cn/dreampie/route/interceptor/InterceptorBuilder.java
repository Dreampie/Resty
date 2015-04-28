package cn.dreampie.route.interceptor;

import cn.dreampie.route.core.Resource;
import cn.dreampie.route.interceptor.annotation.ClearInterceptors;
import cn.dreampie.route.interceptor.annotation.Interceptors;
import cn.dreampie.route.interceptor.exception.InterceptorException;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * InterceptorBuilder
 */
public class InterceptorBuilder {
  private static final Interceptor[] NULL_INTERCEPTOR_ARRAY = new Interceptor[0];
  private Map<Class<Interceptor>, Interceptor> intersMap = new HashMap<Class<Interceptor>, Interceptor>();

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
    for (Interceptor defaultInter : defaultInters) {
      allInters[index++] = defaultInter;
    }
    for (Interceptor resourceInter : resourceInters) {
      allInters[index++] = resourceInter;
    }
    for (Interceptor methodInter : methodInters) {
      allInters[index++] = methodInter;
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
          throw new InterceptorException(e.getMessage(), e);
        }
      }
    }
    return result;
  }
}




