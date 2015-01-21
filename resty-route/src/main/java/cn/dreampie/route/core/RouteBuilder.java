package cn.dreampie.route.core;

import cn.dreampie.route.config.InterceptorLoader;
import cn.dreampie.route.config.ResourceLoader;
import cn.dreampie.route.core.annotation.*;
import cn.dreampie.route.interceptor.Interceptor;
import cn.dreampie.route.interceptor.InterceptorBuilder;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * ActionMapping
 */
public final class RouteBuilder {

  private ResourceLoader resourceLoader;
  private InterceptorLoader interceptorLoader;

  private List<Route> routes = new ArrayList<Route>();

  public RouteBuilder(ResourceLoader resourceLoader, InterceptorLoader interceptorLoader) {
    this.resourceLoader = resourceLoader;
    this.interceptorLoader = interceptorLoader;
  }

  public void addRoute(Route route) {
    for (Route r : routes) {
      if (r.getHttpMethod().equals(route.getHttpMethod()) && r.getPattern().equals(route.getPattern())) {
        throw new RuntimeException("Same path pattern " + r.getHttpMethod() + " " + r.getPattern() + " (" + r.getPathPattern() + " = " + route.getPathPattern() + ")");
      }
    }
    routes.add(route);
  }

  public void build() {
    InterceptorBuilder interceptorBuilder = new InterceptorBuilder();
    Interceptor[] defaultInters = interceptorLoader.getInterceptorArray();
    interceptorBuilder.addToInterceptorsMap(defaultInters);

    DELETE delete = null;
    GET get = null;
    POST post = null;
    PUT put = null;
    HEAD head = null;
    PATCH patch = null;
    String apiPath = "";

    Interceptor[] methodInters;
    Interceptor[] routeInters;
    //addResources
    for (Class<? extends Resource> resourceClazz : resourceLoader.getResources()) {
      Interceptor[] resourceInters = interceptorBuilder.buildResourceInterceptors(resourceClazz);
      apiPath = getApi(resourceClazz);
      //自己的方法
      Method[] methods = resourceClazz.getDeclaredMethods();
      for (Method method : methods) {

        methodInters = interceptorBuilder.buildMethodInterceptors(method);
        routeInters = interceptorBuilder.buildRouteInterceptors(defaultInters, resourceInters, resourceClazz, methodInters, method);

        delete = method.getAnnotation(DELETE.class);
        if (delete != null) {
          addRoute(new Route(resourceClazz, "DELETE", apiPath + delete.value(), method, routeInters));
          continue;
        }

        get = method.getAnnotation(GET.class);
        if (get != null) {
          addRoute(new Route(resourceClazz, "GET", apiPath + get.value(), method, routeInters));
          continue;
        }

        post = method.getAnnotation(POST.class);
        if (post != null) {
          addRoute(new Route(resourceClazz, "POST", apiPath + post.value(), method, routeInters));
          continue;
        }

        put = method.getAnnotation(PUT.class);
        if (put != null) {
          addRoute(new Route(resourceClazz, "PUT", apiPath + put.value(), method, routeInters));
          continue;
        }

        head = method.getAnnotation(HEAD.class);
        if (head != null) {
          addRoute(new Route(resourceClazz, "HEAD", apiPath + head.value(), method, routeInters));
          continue;
        }

        patch = method.getAnnotation(PATCH.class);
        if (patch != null) {
          addRoute(new Route(resourceClazz, "PATCH", apiPath + patch.value(), method, routeInters));
          continue;
        }
      }
    }
    //对routes排序
    Collections.sort(routes, new Comparator() {
      public int compare(Object a, Object b) {
        String one = ((Route) a).getPattern().replace("/" + Route.DEFAULT_PATTERN, "");
        String two = ((Route) b).getPattern().replace("/" + Route.DEFAULT_PATTERN, "");
        int result = two.length() - one.length();
        if (result == 0) {
          return one.compareTo(two);
        }
        return result;
      }
    });

  }

  private String getApi(Class<? extends Resource> resourceClazz) {
    API api;
    String apiPath = "";
    api = resourceClazz.getAnnotation(API.class);
    if (api != null) {
      apiPath = api.value();
    }
    Class<?> superClazz = resourceClazz.getSuperclass();
    if (Resource.class.isAssignableFrom(superClazz)) {
      apiPath = getApi((Class<? extends Resource>) superClazz) + apiPath;
    }
    return apiPath;
  }

  public List<Route> getRoutes() {
    return routes;
  }
}





