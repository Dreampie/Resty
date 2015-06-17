package cn.dreampie.route.core;

import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.common.http.exception.WebException;
import cn.dreampie.common.http.result.HttpStatus;
import cn.dreampie.common.http.result.ImageResult;
import cn.dreampie.common.http.result.WebResult;
import cn.dreampie.common.ioc.ApplicationContainer;
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

/**
 * ActionInvocation invoke the action
 */
public class RouteInvocation {

  private final static Logger logger = Logger.getLogger(RouteInvocation.class);
  private Route route;
  private RouteMatcher routeMatcher;
  private Interceptor[] interceptors;
  private int index = 0;

  // ActionInvocationWrapper need this constructor
  private RouteInvocation() {

  }

  public RouteInvocation(Route route, RouteMatcher routeMatcher) {
    this.route = route;
    this.routeMatcher = routeMatcher;
    this.interceptors = route.getInterceptors();
  }

  /**
   * Invoke the route.
   */
  public void invoke() {
    if (index < interceptors.length) {
      interceptors[index++].intercept(this);
    } else if (index++ == interceptors.length) {
      Object resource = null;
      try {
        //初始化resource
        resource = ApplicationContainer.get(route.getResourceClass());

        //获取所有参数
        Params params = routeMatcher.getParams();

        //数据验证
        validate(params);
        Method method = route.getMethod();
        method.setAccessible(true);
        Object invokeResult;
        //执行方法
        if (route.getAllParamNames().size() > 0) {
          List<String> allParamNames = route.getAllParamNames();
          List<Class<?>> allParamTypes = route.getAllParamTypes();
          int i = 0;
          for (Class<?> type : allParamTypes) {
            if (RouteMatcher.class.isAssignableFrom(type)) {
              break;
            }
            i++;
          }
          //执行方法的参数
          Object[] args = new Object[allParamNames.size()];

          int j = 0;
          for (String name : allParamNames) {
            if (j == i) {
              args[j++] = routeMatcher;
            } else {
              args[j++] = params.get(name);
            }
          }
          invokeResult = method.invoke(resource, args);
        } else {
          invokeResult = method.invoke(resource);
        }
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
  private void render(Object invokeResult) {
    Object result = null;
    HttpRequest request = routeMatcher.getRequest();
    HttpResponse response = routeMatcher.getResponse();
    //通过特定的webresult返回并携带状态码
    if (invokeResult instanceof WebResult) {
      WebResult webResult = (WebResult) invokeResult;
      response.setStatus(webResult.getStatus());
      result = webResult.getResult();
    } else {
      result = invokeResult;
    }
    String extension = routeMatcher.getExtension();
    //file render
    if ((result instanceof File && extension.equals("")) || extension.equals(RenderFactory.FILE)) {
      RenderFactory.getFileRender().render(request, response, result);
    } else
      //image render
      if (((result instanceof ImageResult || result instanceof RenderedImage) && extension.equals(""))
          || extension.equals(RenderFactory.IMAGE)) {
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

  public RouteMatcher getRouteMatcher() {
    return routeMatcher;
  }
}
