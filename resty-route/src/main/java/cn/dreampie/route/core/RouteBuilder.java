package cn.dreampie.route.core;

import cn.dreampie.route.config.InterceptorLoader;
import cn.dreampie.route.config.ResourceLoader;
import cn.dreampie.route.interceptor.Interceptor;
import cn.dreampie.route.interceptor.InterceptorBuilder;
import cn.dreampie.route.core.annotation.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
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

  public void build() {
    InterceptorBuilder interceptorBuilder = new InterceptorBuilder();
    Interceptor[] defaultInters = interceptorLoader.getInterceptorArray();
    interceptorBuilder.addToInterceptorsMap(defaultInters);

    List<Route> matchBuilder = new ArrayList<Route>();

    Resource resource = null;
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
    for (Class<? extends cn.dreampie.route.core.base.Resource> resourceClazz : resourceLoader.getResources()) {
      Interceptor[] resourceInters = interceptorBuilder.buildResourceInterceptors(resourceClazz);
      resource = resourceClazz.getAnnotation(Resource.class);
      if (resource != null) {
        apiPath = resource.value();
      } else {
        apiPath = "";
      }

      Method[] methods = resourceClazz.getMethods();
      for (Method method : methods) {

        methodInters = interceptorBuilder.buildMethodInterceptors(method);
        routeInters = interceptorBuilder.buildRouteInterceptors(defaultInters, resourceInters, resourceClazz, methodInters, method);

        delete = method.getAnnotation(DELETE.class);
        if (delete != null) {
          routes.add(new Route(resourceClazz, "DELETE", apiPath + delete.value(), method, routeInters));
          continue;
        }

        get = method.getAnnotation(GET.class);
        if (get != null) {
          routes.add(new Route(resourceClazz, "GET", apiPath + get.value(), method, routeInters));
          continue;
        }

        post = method.getAnnotation(POST.class);
        if (post != null) {
          routes.add(new Route(resourceClazz, "POST", apiPath + post.value(), method, routeInters));
          continue;
        }

        put = method.getAnnotation(PUT.class);
        if (put != null) {
          routes.add(new Route(resourceClazz, "PUT", apiPath + put.value(), method, routeInters));
          continue;
        }

        head = method.getAnnotation(HEAD.class);
        if (head != null) {
          routes.add(new Route(resourceClazz, "HEAD", apiPath + head.value(), method, routeInters));
          continue;
        }

        patch = method.getAnnotation(PATCH.class);
        if (patch != null) {
          routes.add(new Route(resourceClazz, "PATCH", apiPath + patch.value(), method, routeInters));
          continue;
        }
      }
    }
  }

  public List<Route> getRoutes() {
    return routes;
  }
}





