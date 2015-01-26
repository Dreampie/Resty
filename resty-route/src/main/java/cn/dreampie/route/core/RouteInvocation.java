package cn.dreampie.route.core;

import cn.dreampie.common.http.HttpStatus;
import cn.dreampie.common.http.exception.WebException;
import cn.dreampie.common.util.json.Jsoner;
import cn.dreampie.common.util.json.ObjectCastException;
import cn.dreampie.log.Logger;
import cn.dreampie.route.interceptor.Interceptor;
import cn.dreampie.route.valid.Valid;

import java.lang.reflect.InvocationTargetException;
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
  private RouteMatch routeMatch;
  private Interceptor[] interceptors;
  private int index = 0;

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
  public Object invoke() {
    Object result = null;
    if (index < interceptors.length)
      result = interceptors[index++].intercept(this);
    else if (index++ == interceptors.length) {
      Resource resource = null;
      try {
        resource = route.getResourceClass().newInstance();
        resource.setRouteMatch(routeMatch);
        //获取所有参数
        Params params = getRouteParams();
        //数据验证
        valid(params);
        //执行方法
        Object[] args = params.getValues();
        route.getMethod().setAccessible(true);
        result = route.getMethod().invoke(resource, args);
      } catch (ObjectCastException e) {
        logger.warn("Argument type convert error - " + e.getMessage());
        throw new WebException("Argument type convert error - " + e.getMessage());
      } catch (InvocationTargetException e) {
        Throwable target = e.getTargetException();
        if (target instanceof WebException) {
          throw (WebException) e.getTargetException();
        } else {
          logger.error("Route invocation error.", e);
          throw new WebException("Route invocation error - " + target.getMessage());
        }
      } catch (InstantiationException e) {
        logger.error("Resource instantiation error.", e);
        throw new WebException("Resource instantiation error - " + e.getMessage());
      } catch (IllegalAccessException e) {
        logger.error("Route method access error.", e);
        throw new WebException("Route method access error - " + e.getMessage());
      }
    }
    return result;
  }

  /**
   * 请求参数验证
   *
   * @param params 参数
   */
  private void valid(Params params) {
    Valid[] valids = route.getValids();

    if (valids.length > 0) {
      Map<String, Object> errors = new HashMap<String, Object>();
      HttpStatus status = HttpStatus.BAD_REQUEST;
      Valid valid;
      for (Valid v : valids) {
        valid = v.newInstance();
        //数据验证
        valid.valid(params);
        errors.putAll(valid.getErrors());
        if (!valid.getStatus().equals(status))
          status = v.getStatus();
      }

      if (errors.size() > 0) {
        throw new WebException(status, errors);
      }
    }
  }

  /**
   * 获取所以的请求参数
   *
   * @return 所有参数
   */
  private Params getRouteParams() {
    Params params = new Params();
    int i = 0;
    Class paraType = null;
    List<String> valueArr = null;
    List<String> allParamNames = route.getAllParamNames();
    List<String> pathParamNames = route.getPathParamNames();
    for (String name : allParamNames) {
      paraType = route.getAllParamTypes().get(i);
      //path里的参数
      if (pathParamNames.contains(name)) {
        if (paraType == String.class) {
          params.set(name, routeMatch.getPathParam(name));
        } else
          params.set(name, Jsoner.parseObject(routeMatch.getPathParam(name), paraType));
      } else {//其他参数
        valueArr = routeMatch.getOtherParam(name);
        if (valueArr != null) {

          if (valueArr.size() == 1) {
            if (paraType == String.class) {
              params.set(name, valueArr.get(0));
            } else
              params.set(name, Jsoner.parseObject(valueArr.get(0), paraType));
          } else {
            params.set(name, Jsoner.parseObject(Jsoner.toJSONString(valueArr), paraType));
          }
        }
      }
      i++;
    }
    return params;
  }

  public Method getMethod() {
    return route.getMethod();
  }

  public RouteMatch getRouteMatch() {
    return routeMatch;
  }
}
