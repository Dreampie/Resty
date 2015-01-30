package cn.dreampie.route.core;

import cn.dreampie.common.Entity;
import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpStatus;
import cn.dreampie.common.http.exception.WebException;
import cn.dreampie.common.util.HttpTyper;
import cn.dreampie.common.util.json.Jsoner;
import cn.dreampie.common.util.json.ModelDeserializer;
import cn.dreampie.common.util.json.ObjectCastException;
import cn.dreampie.common.util.stream.StreamReader;
import cn.dreampie.log.Logger;
import cn.dreampie.route.interceptor.Interceptor;
import cn.dreampie.route.valid.Valid;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

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
        Params params = null;
        //if use application/json to post
        HttpRequest request = routeMatch.getRequest();
        //判断是否是application/json 传递数据的
        String contentType = request.getContentType();
        if (contentType != null && contentType.toLowerCase().contains(HttpTyper.ContentType.JSON.value())) {
          params = getJsonParams(request);
        } else {
          params = getFormParams();
        }

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
   * 获取所有的请求参数
   *
   * @return 所有参数
   */
  private Params getFormParams() {
    Params params = new Params();
    int i = 0;
    Class paramType = null;
    List<String> valueArr = null;
    String value = null;
    List<String> allParamNames = route.getAllParamNames();
    List<String> pathParamNames = route.getPathParamNames();

    Object obj = null;

    for (String name : allParamNames) {
      paramType = route.getAllParamTypes().get(i);

      //path里的参数
      if (pathParamNames.contains(name)) {
        if (paramType == String.class) {
          params.set(name, routeMatch.getPathParam(name));
        } else
          params.set(name, Jsoner.parseObject(routeMatch.getPathParam(name), paramType));
      } else {//其他参数
        valueArr = routeMatch.getOtherParam(name);
        if (valueArr != null && valueArr.size() > 0) {
          //不支持数组参数
          value = valueArr.get(0);
          if (paramType == String.class) {
            params.set(name, value);
          } else {
            obj = Jsoner.parseObject(value, paramType);
            //转换为对应的对象类型
            parse(params, i, paramType, obj, name);
          }
        }
      }
      i++;
    }
    return params;
  }

  /**
   * 获取所有以application/json方式提交的数据
   *
   * @param request request对象
   * @return 所有参数
   */
  private Params getJsonParams(HttpRequest request) {
    Params params = new Params();
    InputStream is = null;

    int i = 0;
    Class paramType = null;
    List<String> allParamNames = route.getAllParamNames();
    List<String> pathParamNames = route.getPathParamNames();

    Object obj = null;
    String json = "";
    try {
      is = request.getContentStream();
      json = StreamReader.readString(is);
    } catch (IOException e) {
      String msg = "Could not read inputStream when contentType is application/json.";
      logger.error(msg, e);
      throw new WebException(msg);
    }
    if (null != json && !"".equals(json)) {
      Map<String, Object> paramsMap = Jsoner.parseObject(json, Map.class);
      for (String name : allParamNames) {
        paramType = route.getAllParamTypes().get(i);

        //path里的参数
        if (pathParamNames.contains(name)) {
          if (paramType == String.class) {
            params.set(name, routeMatch.getPathParam(name));
          } else
            params.set(name, Jsoner.parseObject(routeMatch.getPathParam(name), paramType));
        } else {//其他参数
          obj = paramsMap.get(name);

          if (paramType == String.class) {
            params.set(name, obj.toString());
          } else {
            //转换对象到指定的类型
            parse(params, i, paramType, obj, name);
          }
        }
        i++;
      }
    }
    return params;
  }

  private void parse(Params params, int i, Class paramType, Object obj, String name) {
    Type genericParamType;
    Class paramTypeClass;
    List<Map<String, Object>> list;
    List<Entity<?>> newlist;
    Entity<?> entity;
    JSONArray blist;
    List<?> newblist;
    Set<Entity<?>> newset;
    JSONArray bset;
    Set<?> newbset;

    if (obj.getClass().isAssignableFrom(paramType)) {
      params.set(name, obj);
    } else {
      //判断参数需要的类型
      //判断是不是包含 Entity类型
      try {
        if (Entity.class.isAssignableFrom(paramType)) {
          Entity<?> e = (Entity<?>) paramType.newInstance();
          e.putAttrs(ModelDeserializer.deserialze((Map<String, Object>) obj, paramType));
          params.set(name, e);
        } else {
          if (Collection.class.isAssignableFrom(paramType)) {
            genericParamType = route.getAllGenericParamTypes().get(i);
            paramTypeClass = (Class) ((ParameterizedType) genericParamType).getActualTypeArguments()[0];

            if (List.class.isAssignableFrom(paramType)) {
              list = (List<Map<String, Object>>) obj;
              //Entity类型
              if (Entity.class.isAssignableFrom(paramTypeClass)) {
                newlist = new ArrayList<Entity<?>>();
                for (Map<String, Object> mp : list) {
                  entity = (Entity<?>) paramTypeClass.newInstance();
                  entity.putAttrs(ModelDeserializer.deserialze(mp, paramTypeClass));
                  newlist.add(entity);
                }
                params.set(name, newlist);
              } else {
                blist = (JSONArray) obj;
                if (String.class.isAssignableFrom(paramTypeClass)) {
                  newblist = new ArrayList<String>();
                  for (Object e : blist) {
                    ((List<String>) newblist).add(e.toString());
                  }
                } else {
                  newblist = new ArrayList<Object>();
                  for (Object e : blist) {
                    if (e.getClass().isAssignableFrom(paramTypeClass))
                      ((List<Object>) newblist).add(e);
                    else
                      ((List<Object>) newblist).add(Jsoner.parseObject(Jsoner.toJSONString(e), paramTypeClass));
                  }
                }
                params.set(name, newblist);
              }
            } else if (Set.class.isAssignableFrom(paramType)) {
              //Entity
              if (Entity.class.isAssignableFrom(paramTypeClass)) {
                list = (List<Map<String, Object>>) obj;
                newset = new HashSet<Entity<?>>();
                for (Map<String, Object> mp : list) {
                  entity = (Entity<?>) paramTypeClass.newInstance();
                  entity.putAttrs(ModelDeserializer.deserialze(mp, paramTypeClass));
                  newset.add(entity);
                }
                params.set(name, newset);
              } else {
                bset = (JSONArray) obj;
                if (String.class.isAssignableFrom(paramTypeClass)) {
                  newbset = new HashSet<String>();
                  for (Object e : bset) {
                    ((Set<String>) newbset).add(e.toString());
                  }
                } else {
                  newbset = new HashSet<Object>();
                  for (Object e : bset) {
                    if (e.getClass().isAssignableFrom(paramTypeClass))
                      ((Set<Object>) newbset).add(e);
                    else
                      ((Set<Object>) newbset).add(Jsoner.parseObject(Jsoner.toJSONString(e), paramTypeClass));
                  }
                }
                params.set(name, newbset);
              }
            }
          } else {
            params.set(name, Jsoner.parseObject(Jsoner.toJSONString(obj), paramType));
          }
        }
      } catch (Exception e) {
        throw new JSONException("Unconvert type " + paramType, e);
      }
    }
  }

  public Method getMethod() {
    return route.getMethod();
  }

  public RouteMatch getRouteMatch() {
    return routeMatch;
  }
}
