package cn.dreampie.core.route;

import cn.dreampie.core.interceptor.Interceptor;
import cn.dreampie.core.invocation.Invocation;
import cn.dreampie.core.route.base.Resource;
import com.alibaba.fastjson.JSON;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * ActionInvocation invoke the action
 */
public class RouteInvocation implements Invocation {

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
      interceptors[index++].intercept(this);
    else if (index++ == interceptors.length) {
      Resource resource = null;
      try {
        resource = route.getResourceClass().newInstance();
        //获取所以参数
        Object[] args = getRouteArgs();
        route.getMethod().setAccessible(true);
        result = route.getMethod().invoke(resource, args);
      } catch (InvocationTargetException e) {
        Throwable cause = e.getTargetException();
        if (cause instanceof RuntimeException)
          throw (RuntimeException) cause;
        throw new RuntimeException(e);
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        throw new RuntimeException(e);
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
          args[i] = JSON.parseObject(routeMatch.getPathParam(name), paraType);
      } else {//其他参数
        valueArr = routeMatch.getOtherParam(name);
        if (valueArr != null) {

          if (valueArr.size() == 1) {
            if (paraType == String.class) {
              args[i] = valueArr.get(0);
            } else
              args[i] = JSON.parseObject(valueArr.get(0), paraType);
          } else {
            args[i] = JSON.parseObject(JSON.toJSONString(valueArr), paraType);
          }
        }
      }
      i++;
    }
    return args;
  }

  public static void main(String[] args) {
    List<String> v = new ArrayList<String>();
    v.add("1");

    List<Integer> result = JSON.parseObject(JSON.toJSONString(v), List.class);

    System.out.println(v.getClass() == ArrayList.class);

    String s = "asdasda.x";
    int index = s.indexOf(".");
    String a = s.substring(index + 1);
    s = s.substring(0, index);
    System.out.println(s + "----" + a);
  }
}
