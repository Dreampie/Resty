package cn.dreampie.route.core;

import cn.dreampie.common.util.analysis.ParamAttribute;
import cn.dreampie.common.util.analysis.ParamNamesScaner;
import cn.dreampie.route.config.InterceptorLoader;
import cn.dreampie.route.config.ResourceLoader;
import cn.dreampie.route.core.annotation.*;
import cn.dreampie.route.core.multipart.FILE;
import cn.dreampie.route.core.multipart.MultipartBuilder;
import cn.dreampie.route.interceptor.Interceptor;
import cn.dreampie.route.interceptor.InterceptorBuilder;
import cn.dreampie.route.valid.Validator;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Route Mapping
 */
public final class RouteBuilder {

  private ResourceLoader resourceLoader;
  private InterceptorLoader interceptorLoader;

  //对routes排序
  private Set<Route> routes = new TreeSet<Route>(
      new Comparator<Route>() {
        public int compare(Route a, Route b) {
          String one = a.getPattern().replace("/" + Route.DEFAULT_PATTERN, "");
          String two = b.getPattern().replace("/" + Route.DEFAULT_PATTERN, "");
          int result = two.length() - one.length();
          if (result == 0) {
            return one.compareTo(two);
          }
          return result;
        }
      });

  public RouteBuilder(ResourceLoader resourceLoader, InterceptorLoader interceptorLoader) {
    this.resourceLoader = resourceLoader;
    this.interceptorLoader = interceptorLoader;
  }

  public void addRoute(Route route) {
    for (Route r : routes) {
      if (r.getHttpMethod().equals(route.getHttpMethod()) && r.getPattern().equals(route.getPattern())) {
        throw new IllegalArgumentException("Same path pattern " + r.getHttpMethod() + " " + r.getPattern());
      }
    }
    routes.add(route);
  }

  public void build() {
    InterceptorBuilder interceptorBuilder = new InterceptorBuilder();
    Interceptor[] defaultInters = interceptorLoader.getInterceptorArray();
    interceptorBuilder.addToInterceptorsMap(defaultInters);

    //文件上传的注解
    FILE file = null;
    DELETE delete = null;
    GET get = null;
    POST post = null;
    PUT put = null;
    HEAD head = null;
    PATCH patch = null;
    String apiPath = "";

    //文件上传构建器
    MultipartBuilder multipartBuilder = null;
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

        //文件上传 构建器
        file = method.getAnnotation(FILE.class);
        if (file != null) {
          multipartBuilder = new MultipartBuilder(file.dir(), file.max(), file.encoding(), file.allows());
        } else {
          multipartBuilder = null;
        }

        //delete 请求
        delete = method.getAnnotation(DELETE.class);
        if (delete != null) {
          validClasses = delete.valid();
          validators = getValidators(validClasses);
          addRoute(new Route(resourceClazz, paramAttribute, "DELETE", apiPath + delete.value(), method, routeInters,
              delete.des(), validators, multipartBuilder));
          continue;
        }
        //get 请求
        get = method.getAnnotation(GET.class);
        if (get != null) {
          validClasses = get.valid();
          validators = getValidators(validClasses);
          addRoute(new Route(resourceClazz, paramAttribute, "GET", apiPath + get.value(), method, routeInters,
              get.des(), validators, multipartBuilder));
          continue;
        }
        //post 请求
        post = method.getAnnotation(POST.class);
        if (post != null) {
          validClasses = post.valid();
          validators = getValidators(validClasses);
          addRoute(new Route(resourceClazz, paramAttribute, "POST", apiPath + post.value(), method, routeInters,
              post.des(), validators, multipartBuilder));
          continue;
        }
        //put 请求
        put = method.getAnnotation(PUT.class);
        if (put != null) {
          validClasses = put.valid();
          validators = getValidators(validClasses);
          addRoute(new Route(resourceClazz, paramAttribute, "PUT", apiPath + put.value(), method, routeInters,
              put.des(), validators, multipartBuilder));
          continue;
        }
        //head 请求
        head = method.getAnnotation(HEAD.class);
        if (head != null) {
          validClasses = head.valid();
          validators = getValidators(validClasses);
          addRoute(new Route(resourceClazz, paramAttribute, "HEAD", apiPath + head.value(), method, routeInters,
              head.des(), validators, multipartBuilder));
          continue;
        }
        //patch 请求
        patch = method.getAnnotation(PATCH.class);
        if (patch != null) {
          validClasses = patch.valid();
          validators = getValidators(validClasses);
          addRoute(new Route(resourceClazz, paramAttribute, "PATCH", apiPath + patch.value(), method, routeInters,
              patch.des(), validators, multipartBuilder));
          continue;
        }
      }
    }
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

  public Set<Route> getRoutes() {
    return routes;
  }
}





