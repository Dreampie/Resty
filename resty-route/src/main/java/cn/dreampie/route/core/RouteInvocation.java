package cn.dreampie.route.core;

import cn.dreampie.common.entity.Entity;
import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.common.http.UploadedFile;
import cn.dreampie.common.http.exception.WebException;
import cn.dreampie.common.http.result.HttpStatus;
import cn.dreampie.common.http.result.ImageResult;
import cn.dreampie.common.http.result.WebResult;
import cn.dreampie.common.util.HttpTyper;
import cn.dreampie.common.util.json.Jsoner;
import cn.dreampie.common.util.json.ModelDeserializer;
import cn.dreampie.common.util.stream.StreamReader;
import cn.dreampie.log.Logger;
import cn.dreampie.route.interceptor.Interceptor;
import cn.dreampie.route.render.RenderFactory;
import cn.dreampie.route.valid.ValidResult;
import cn.dreampie.route.valid.Validator;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
  public void invoke() {
    if (index < interceptors.length) {
      interceptors[index++].intercept(this);
    } else if (index++ == interceptors.length) {
      Resource resource = null;
      try {
        //初始化resource
        resource = route.getResourceClass().newInstance();
        resource.setRouteMatch(routeMatch);

        //获取所有参数
        Params params = null;
        HttpRequest request = routeMatch.getRequest();
        //if use application/json to post
        //判断是否是application/json 传递数据的
        String contentType = request.getContentType();
        if (contentType != null && contentType.toLowerCase().contains(HttpTyper.ContentType.JSON.value())) {
          params = getJsonParams(request);
        } else {
          params = getFormParams();
        }

        //数据验证
        validate(params);
        //执行方法的参数
        Object[] args = params.getValues();
        route.getMethod().setAccessible(true);

        //执行方法
        Object invokeResult = route.getMethod().invoke(resource, args);
        //输出结果
        render(invokeResult);
      } catch (Exception e) {
        Throwable cause = e.getCause();
        if (cause != null) {
          throwException(cause);
        } else {
          throwException(e);
        }
      }
    }
  }

  /**
   * 抛出异常
   *
   * @param cause
   */
  private void throwException(Throwable cause) {
    if (cause instanceof WebException) {
      throw (WebException) cause;
    } else {
      logger.error("Route method invoke error.", cause);
      throw new WebException(cause.getMessage());
    }
  }

  /**
   * 输出内容
   *
   * @param invokeResult invokeResult
   */
  private void render(Object invokeResult) {
    Object result = null;
    HttpRequest request = routeMatch.getRequest();
    HttpResponse response = routeMatch.getResponse();
    //通过特定的webresult返回并携带状态码
    if (invokeResult instanceof WebResult) {
      WebResult webResult = (WebResult) invokeResult;
      response.setStatus(webResult.getStatus());
      result = webResult.getResult();
    } else {
      result = invokeResult;
    }
    String extension = routeMatch.getExtension();
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
      HttpStatus status = HttpStatus.BAD_REQUEST;
      ValidResult vr;

      for (Validator validator : validators) {
        //数据验证
        vr = validator.validate(params);
        errors.putAll(vr.getErrors());
        if (!vr.getStatus().equals(status))
          status = vr.getStatus();
      }

      if (errors.size() > 0) {
        throw new WebException(status, errors);
      }
    }
  }

  /**
   * 转换string类型参数
   *
   * @param params
   * @param i
   * @param paramType
   * @param valueArr
   * @param name
   */
  private void parseString(Params params, int i, Class paramType, List<String> valueArr, String name) {
    String value;
    Object obj;
    if (valueArr != null && valueArr.size() > 0) {
      //不支持数组参数
      value = valueArr.get(0);
      if (paramType == String.class) {
        params.set(name, value);
      } else {
        obj = Jsoner.toObject(value, paramType);
        if (obj == null) {
          params.set(name, null);
        } else {
          //转换为对应的对象类型
          parse(params, i, paramType, obj, name);
        }
      }
    } else {
      params.set(name, null);
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

    //判断范型类型
    Type[] typeArguments;

    Class keyTypeClass;
    Class valueTypeClass;
    for (String name : allParamNames) {
      paramType = route.getAllParamTypes().get(i);

      //path里的参数
      if (pathParamNames.contains(name)) {
        if (paramType == String.class) {
          params.set(name, routeMatch.getPathParam(name));
        } else
          params.set(name, Jsoner.toObject(routeMatch.getPathParam(name), paramType));
      } else {//其他参数
        if (paramType == UploadedFile.class) {
          params.set(name, routeMatch.getFileParam(name));
        } else if (paramType == Map.class) {
          typeArguments = ((ParameterizedType) route.getAllGenericParamTypes().get(i)).getActualTypeArguments();
          if (typeArguments.length >= 2) {
            keyTypeClass = (Class) typeArguments[0];
            valueTypeClass = (Class) typeArguments[1];
            if (keyTypeClass == String.class && valueTypeClass == UploadedFile.class) {
              params.set(name, routeMatch.getFileParams());
            } else {
              valueArr = routeMatch.getOtherParam(name);
              parseString(params, i, paramType, valueArr, name);
            }
          } else {
            valueArr = routeMatch.getOtherParam(name);
            parseString(params, i, paramType, valueArr, name);
          }
        } else {
          valueArr = routeMatch.getOtherParam(name);
          parseString(params, i, paramType, valueArr, name);
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
    boolean hasJsonParam = null != json && !"".equals(json);
    Map<String, Object> paramsMap = null;
    if (hasJsonParam) {
      paramsMap = Jsoner.toObject(json, Map.class);
      hasJsonParam = paramsMap != null && paramsMap.size() > 0;
    }
    for (String name : allParamNames) {
      paramType = route.getAllParamTypes().get(i);

      //path里的参数
      if (pathParamNames.contains(name)) {
        if (paramType == String.class) {
          params.set(name, routeMatch.getPathParam(name));
        } else {
          params.set(name, Jsoner.toObject(routeMatch.getPathParam(name), paramType));
        }
      } else {//其他参数
        if (hasJsonParam) {
          obj = paramsMap.get(name);

          if (obj != null) {
            if (paramType == String.class) {
              params.set(name, obj.toString());
            } else {
              //转换对象到指定的类型
              parse(params, i, paramType, obj, name);
            }
          } else {
            params.set(name, null);
          }
        } else {
          params.set(name, null);
        }
      }
      i++;
    }
    return params;
  }

  /**
   * 把参数转到对应的类型
   *
   * @param params
   * @param i
   * @param paramType
   * @param obj
   * @param name
   */
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

    if (obj == null) {
      params.set(name, null);
    } else if (paramType.isAssignableFrom(obj.getClass())) {
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
                if (String.class == paramTypeClass) {
                  newblist = new ArrayList<String>();
                  for (Object e : blist) {
                    ((List<String>) newblist).add(e.toString());
                  }
                } else {
                  newblist = new ArrayList<Object>();
                  for (Object e : blist) {
                    if (paramTypeClass.isAssignableFrom(e.getClass()))
                      ((List<Object>) newblist).add(e);
                    else
                      ((List<Object>) newblist).add(Jsoner.toObject(Jsoner.toJSON(e), paramTypeClass));
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
                if (String.class == paramTypeClass) {
                  newbset = new HashSet<String>();
                  for (Object e : bset) {
                    ((Set<String>) newbset).add(e.toString());
                  }
                } else {
                  newbset = new HashSet<Object>();
                  for (Object e : bset) {
                    if (paramTypeClass.isAssignableFrom(e.getClass()))
                      ((Set<Object>) newbset).add(e);
                    else
                      ((Set<Object>) newbset).add(Jsoner.toObject(Jsoner.toJSON(e), paramTypeClass));
                  }
                }
                params.set(name, newbset);
              }
            }
          } else {
            params.set(name, Jsoner.toObject(Jsoner.toJSON(obj), paramType));
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
