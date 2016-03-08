package cn.dreampie.route.core;

import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.common.http.exception.WebException;
import cn.dreampie.common.http.result.HttpStatus;
import cn.dreampie.common.http.result.WebResult;
import cn.dreampie.common.spring.SpringBuilder;
import cn.dreampie.common.spring.SpringHolder;
import cn.dreampie.log.Logger;
import cn.dreampie.route.interceptor.Interceptor;
import cn.dreampie.route.render.RenderFactory;
import cn.dreampie.route.valid.ValidResult;
import cn.dreampie.route.valid.Validator;

import java.awt.image.RenderedImage;
import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.dreampie.common.util.Checker.checkNotNull;

/**
 * ActionInvocation invoke the action
 */
public class RouteInvocation {

  private final static Logger logger = Logger.getLogger(RouteInvocation.class);
  private Route route;
  private RouteMatch routeMatch;
  private Interceptor[] interceptors;
  private int index = 0;
  private boolean wasInvoke = false;
  private Object invokeResult = null;

  // ActionInvocationWrapper need this constructor
  private RouteInvocation() {

  }

  public RouteInvocation(Route route, RouteMatch routeMatch) {
    this.route = route;
    this.routeMatch = routeMatch;
    this.interceptors = route.getInterceptors();
  }

  /**
   * Invoke the route.
   */
  private void methodInvoke() {
    if (index < interceptors.length) {
      interceptors[index++].intercept(this);
    } else if (index++ == interceptors.length) {
      Resource resource;
      try {
        //初始化resource
        if (SpringHolder.alive) {
          resource = SpringBuilder.getBean(route.getResourceClass());
        } else {
          resource = route.getResourceClass().newInstance();
        }

        checkNotNull(resource, "Could init '" + route.getResourceClass() + "' before invoke method.");
        resource.setRouteMatch(routeMatch);
        //获取所有参数
        Params params = routeMatch.getParams();

        //数据验证
        validate(params);
        Method method = route.getMethod();
        method.setAccessible(true);
        //执行方法
        if (route.getAllParamNames().size() > 0) {
          List<Class<?>> allParamTypes = route.getAllParamTypes();
          List<String> allParamNames = route.getAllParamNames();
          //执行方法的参数
          Object[] args = new Object[allParamNames.size()];
          int i = 0;
          for (String name : allParamNames) {
            if (HttpRequest.class.isAssignableFrom(allParamTypes.get(i))) {
              args[i++] = routeMatch.getRequest();
            } else if (HttpResponse.class.isAssignableFrom(allParamTypes.get(i))) {
              args[i++] = routeMatch.getResponse();
            } else if (Headers.class.isAssignableFrom(allParamTypes.get(i))) {
              args[i++] = routeMatch.getHeaders();
            } else if (Params.class.isAssignableFrom(allParamTypes.get(i))) {
              args[i++] = routeMatch.getParams();
            } else {
              args[i++] = params.get(name);
            }
          }
          invokeResult = method.invoke(resource, args);
        } else {
          invokeResult = method.invoke(resource);
        }
        wasInvoke = true;
        //输出结果
        render(invokeResult);
      } catch (Exception e) {
        route.throwException(e);
      }
    }
  }


  /**
   * 输出内容
   *
   * @param invokeResult invokeResult
   */
  public void render(Object invokeResult) {
    Object result;
    HttpRequest request = routeMatch.getRequest();
    HttpResponse response = routeMatch.getResponse();
    //通过特定的webresult返回并携带状态码
    if (invokeResult instanceof WebResult) {
      WebResult webResult = (WebResult) invokeResult;
      response.setStatus(webResult.getStatus());
      Map<String, String> headers = webResult.getHeaders();
      if (headers != null && headers.size() > 0) {
        for (Map.Entry<String, String> headersEntry : headers.entrySet()) {
          response.setHeader(headersEntry.getKey(), headersEntry.getValue());
        }
      }
      result = webResult.getResult();
    } else {
      result = invokeResult;
    }
    String extension = routeMatch.getExtension();
    //file render
    if ((result instanceof File && extension.equals("")) || extension.equals(RenderFactory.FILE)) {
      RenderFactory.getFileRender().render(request, response, result);
    } else if ((result instanceof RenderedImage && extension.equals("")) || extension.equals(RenderFactory.IMAGE)) {
      //如果是string  表示为文件类型
      if (result instanceof String) {
        RenderFactory.getFileRender().render(request, response, result);
      } else {
        RenderFactory.getImageRender().render(request, response, result);
      }
    } else {
      RenderFactory.get(extension).render(request, response, result);
    }
  }

  /**
   * 请求参数验证
   *
   * @param params 参数
   */
  private void validate(Params params) {
    Validator[] validators = route.getValidators();

    if (validators.length > 0) {
      Map<String, Object> errors = new HashMap<String, Object>();
      HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
      ValidResult vr;

      for (Validator validator : validators) {
        //数据验证
        vr = validator.validate(params);
        errors.putAll(vr.getErrors());
        if (!status.equals(vr.getStatus()))
          status = vr.getStatus();
      }

      if (errors.size() > 0) {
        throw new WebException(status, errors);
      }
    }
  }


  public Method getMethod() {
    return route.getMethod();
  }

  public Class getResourceClass() {
    return route.getResourceClass();
  }

  public RouteMatch getRouteMatch() {
    return routeMatch;
  }

  public Object invoke() {
    if (!wasInvoke) {
      methodInvoke();
    }
    return invokeResult;
  }
}
