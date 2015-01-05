package cn.dreampie.route.core;

import cn.dreampie.log.Logger;
import cn.dreampie.log.LoggerFactory;
import cn.dreampie.route.core.base.Resource;
import cn.dreampie.route.http.exception.WebException;
import cn.dreampie.route.interceptor.Interceptor;
import cn.dreampie.route.invocation.Invocation;
import cn.dreampie.util.json.Jsoner;
import com.alibaba.fastjson.JSONException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * ActionInvocation invoke the action
 */
public class RouteInvocation implements Invocation {

  private final static Logger logger = LoggerFactory.getLogger(RouteInvocation.class);
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
   * Invoke the action.
   */
  public Object invoke() {
    Object result = null;
    if (index < interceptors.length)
      result = interceptors[index++].intercept(this);
    else if (index++ == interceptors.length) {
      Resource resource = null;
      try {
        resource = route.getResourceClass().newInstance();
        //获取所以参数
        Object[] args = getRouteArgs();
        route.getMethod().setAccessible(true);
        result = route.getMethod().invoke(resource, args);
      } catch (ClassCastException e) {
        throw new WebException(e.getMessage());
      } catch (JSONException e) {
        throw new WebException(e.getMessage());
      } catch (InvocationTargetException e) {
        logger.error("Route invocation error.", e);
      } catch (InstantiationException e) {
        logger.error("Resource instantiation error.", e);
      } catch (IllegalAccessException e) {
        logger.error("Route method access error.", e);
      }
    }
    return result;
  }

  private Object[] getRouteArgs() {
    Object[] args = new Object[route.getAllParamNames().size()];
    int i = 0;
    Class paraType = null;
    List<String> valueArr = null;
    for (String name : route.getAllParamNames()) {
      paraType = route.getAllParamTypes().get(i);
      //path里的参数
      if (route.getPathParamNames().contains(name)) {
        if (paraType == String.class) {
          args[i] = routeMatch.getPathParam(name);
        } else
          args[i] = Jsoner.parseObject(routeMatch.getPathParam(name), paraType);
      } else {//其他参数
        valueArr = routeMatch.getOtherParam(name);
        if (valueArr != null) {

          if (valueArr.size() == 1) {
            if (paraType == String.class) {
              args[i] = valueArr.get(0);
            } else
              args[i] = Jsoner.parseObject(valueArr.get(0), paraType);
          } else {
            args[i] = Jsoner.parseObject(Jsoner.toJSONString(valueArr), paraType);
          }
        }
      }
      i++;
    }
    return args;
  }

  public Method getMethod() {
    return route.getMethod();
  }
}
