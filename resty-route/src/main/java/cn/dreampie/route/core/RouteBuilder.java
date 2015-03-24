package cn.dreampie.route.core;

import cn.dreampie.common.util.analysis.ParamAttribute;
import cn.dreampie.common.util.analysis.ParamNamesScaner;
import cn.dreampie.route.config.InterceptorLoader;
import cn.dreampie.route.config.ResourceLoader;
import cn.dreampie.route.core.annotation.*;
import cn.dreampie.route.interceptor.Interceptor;
import cn.dreampie.route.interceptor.InterceptorBuilder;
import cn.dreampie.route.valid.Validator;

import java.lang.reflect.Method;
import java.util.*;

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
    //当前路由所有的拦截器 包括resource的和method的
    Interceptor[] routeInters;
    //resource的拦截器
    Interceptor[] resourceInters;
    //获取参数
    Map<String, ParamAttribute> classParamNames;
    //当前resource的方法
    Method[] methods;
    //当前方法的参数属性
    ParamAttribute paramAttribute;

    //validate
    Class<? extends Validator>[] validClasses;
    Validator[] validators;
    //addResources
    for (Class<? extends Resource> resourceClazz : resourceLoader.getResources()) {
      resourceInters = interceptorBuilder.buildResourceInterceptors(resourceClazz);
      classParamNames = ParamNamesScaner.getParamNames(resourceClazz);

      apiPath = getApi(resourceClazz);
      //自己的方法
      methods = resourceClazz.getDeclaredMethods();
      for (Method method : methods) {

        paramAttribute = ParamNamesScaner.getParamNames(method, classParamNames);
        methodInters = interceptorBuilder.buildMethodInterceptors(method);
        routeInters = interceptorBuilder.buildRouteInterceptors(defaultInters, resourceInters, resourceClazz, methodInters, method);

        delete = method.getAnnotation(DELETE.class);
        if (delete != null) {
          validClasses = delete.valid();
          validators = getValidators(validClasses);
          addRoute(new Route(resourceClazz, paramAttribute, "DELETE", apiPath + delete.value(), method, routeInters, delete.des(), validators));
          continue;
        }

        get = method.getAnnotation(GET.class);
        if (get != null) {
          validClasses = get.valid();
          validators = getValidators(validClasses);
          addRoute(new Route(resourceClazz, paramAttribute, "GET", apiPath + get.value(), method, routeInters, get.des(), validators));
          continue;
        }

        post = method.getAnnotation(POST.class);
        if (post != null) {
          validClasses = post.valid();
          validators = getValidators(validClasses);
          addRoute(new Route(resourceClazz, paramAttribute, "POST", apiPath + post.value(), method, routeInters, post.des(), validators));
          continue;
        }

        put = method.getAnnotation(PUT.class);
        if (put != null) {
          validClasses = put.valid();
          validators = getValidators(validClasses);
          addRoute(new Route(resourceClazz, paramAttribute, "PUT", apiPath + put.value(), method, routeInters, put.des(), validators));
          continue;
        }

        head = method.getAnnotation(HEAD.class);
        if (head != null) {
          validClasses = head.valid();
          validators = getValidators(validClasses);
          addRoute(new Route(resourceClazz, paramAttribute, "HEAD", apiPath + head.value(), method, routeInters, head.des(), validators));
          continue;
        }

        patch = method.getAnnotation(PATCH.class);
        if (patch != null) {
          validClasses = patch.valid();
          validators = getValidators(validClasses);
          addRoute(new Route(resourceClazz, paramAttribute, "PATCH", apiPath + patch.value(), method, routeInters, patch.des(), validators));
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

  /**
   * 获取所有验证器
   *
   * @param validClasses 验证器的class
   * @return Valid[]
   */
  private Validator[] getValidators(Class<? extends Validator>[] validClasses) {
    Validator[] validators = new Validator[validClasses.length];
    if (validClasses.length > 0) {
      int i = 0;
      for (Class<? extends Validator> valid : validClasses) {
        try {
          validators[i] = valid.newInstance();
        } catch (InstantiationException e) {
          throw new RuntimeException(e.getMessage(), e);
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
        i++;
      }
    }
    return validators;
  }

  /**
   * 获取api部分
   *
   * @param resourceClazz resource class
   * @return url
   */
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





