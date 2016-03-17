package cn.dreampie.route.core;


import cn.dreampie.common.Constant;
import cn.dreampie.common.entity.Entity;
import cn.dreampie.common.http.*;
import cn.dreampie.common.http.exception.WebException;
import cn.dreampie.common.http.result.HttpStatus;
import cn.dreampie.common.util.Joiner;
import cn.dreampie.common.util.analysis.ParamAttribute;
import cn.dreampie.common.util.analysis.ParamNamesScaner;
import cn.dreampie.common.util.json.Jsoner;
import cn.dreampie.common.util.json.ModelDeserializer;
import cn.dreampie.common.util.stream.StreamReader;
import cn.dreampie.log.Logger;
import cn.dreampie.route.core.multipart.MultipartBuilder;
import cn.dreampie.route.core.multipart.MultipartParam;
import cn.dreampie.route.exception.InitException;
import cn.dreampie.route.interceptor.Interceptor;
import cn.dreampie.route.render.RenderFactory;
import cn.dreampie.route.valid.Validator;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.dreampie.common.util.Checker.checkArgument;
import static cn.dreampie.common.util.Checker.checkNotNull;

/**
 * Created by ice on 14-12-19.
 */
public class Route {

  public static final String PARAM_PATTERN = "([^\\/]+)";
  private static final Logger logger = Logger.getLogger(Route.class);
  private static final PathParserCharProcessor regularCharPathParserCharProcessor = new PathParserCharProcessor() {

    public void handle(int curChar, PathPatternParser pathPatternParser) {
      if (curChar == '{') {
        pathPatternParser.processor = new CurlyBracesPathParamPathParserCharProcessor();
      } else if (curChar == ':') {
        pathPatternParser.processor = new SimpleColumnBasedPathParamParserCharProcessor();
      } else {
        pathPatternParser.patternBuilder.appendCodePoint(curChar);
        pathPatternParser.stdPathPatternBuilder.appendCodePoint(curChar);
      }
    }

    public void end(PathPatternParser pathPatternParser) {
    }
  };
  private final String httpMethod;
  private final String pathPattern;
  private final String stdPathPattern;
  private final Pattern pattern;
  private final List<String> pathParamNames;
  private final Class<? extends Resource> resourceClass;
  private final Method method;
  private final List<String> allParamNames;
  private final int[] allLineNumbers;
  private final List<Class<?>> allParamTypes;
  private final List<Type> allGenericParamTypes;
  private final Interceptor[] interceptors;
  private final int[][] interceptorsLineNumbers;
  private final Validator[] validators;
  private final int[][] validsLineNumbers;
  private final MultipartBuilder multipartBuilder;
  private final Map<String, String> headers;

  public Route(Class<? extends Resource> resourceClass, ParamAttribute paramAttribute, String httpMethod, String pathPattern, Method method, Interceptor[] interceptors, String des, Validator[] validators, MultipartBuilder multipartBuilder, Map<String, String> headers) {
    this.resourceClass = resourceClass;
    this.httpMethod = checkNotNull(httpMethod);
    this.pathPattern = checkNotNull(pathPattern);
    this.method = method;
    this.multipartBuilder = multipartBuilder;
    this.interceptors = interceptors;

    this.allParamNames = paramAttribute.getNames();
    this.allLineNumbers = paramAttribute.getLines();
    this.allParamTypes = Arrays.asList(method.getParameterTypes());
    this.allGenericParamTypes = Arrays.asList(method.getGenericParameterTypes());
    this.validators = validators;
    this.headers = headers;
    //获取拦截器的行号
    if (Constant.showRoute) {
      this.interceptorsLineNumbers = new int[interceptors.length][];
      //获取参数
      ParamAttribute paramAttr;
      int i = 0;
      for (Interceptor interceptor : interceptors) {
        try {
          paramAttr = ParamNamesScaner.getParamNames(interceptor.getClass().getMethod("intercept", RouteInvocation.class));
        } catch (NoSuchMethodException e) {
          throw new InitException(e.getMessage(), e);
        }
        this.interceptorsLineNumbers[i] = paramAttr.getLines();
        i++;
      }
      //验证器
      this.validsLineNumbers = new int[validators.length][];
      i = 0;
      for (Validator validator : validators) {
        try {
          paramAttr = ParamNamesScaner.getParamNames(validator.getClass().getMethod("validate", Params.class));
        } catch (NoSuchMethodException e) {
          throw new InitException(e.getMessage(), e);
        }
        this.validsLineNumbers[i] = paramAttr.getLines();
        i++;
      }
    } else {
      this.validsLineNumbers = null;
      this.interceptorsLineNumbers = null;
    }
    PathPatternParser s = new PathPatternParser(pathPattern);
    s.parse();

    this.pattern = Pattern.compile(s.patternBuilder.toString());
    this.stdPathPattern = s.stdPathPatternBuilder.toString();
    this.pathParamNames = s.pathParamNames;
    //check arguments
    for (String pName : pathParamNames) {
      if (!allParamNames.contains(pName)) {
        throw new IllegalArgumentException("PathParameter '" + pName + "' could not found in method arguments at " + resourceClass.getName() + "(" + resourceClass.getSimpleName() + ".java:" + allLineNumbers[0] + ")");
      }
    }

    if (logger.isInfoEnabled()) {
      //print route
      StringBuilder sb = new StringBuilder("\n\nBuild route ----------------- ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append(" ------------------------------");
      sb.append("\nResource     : ").append(resourceClass.getName()).append("(").append(resourceClass.getSimpleName()).append(".java:").append(allLineNumbers[0]).append(")");
      sb.append("\nMethod       : ").append(method.getName());
      sb.append("\nPathPattern  : ").append(httpMethod).append(" ").append(pathPattern);
      //print params
      StringBuilder sbPath = new StringBuilder("\nPathParams   : ");
      StringBuilder sbOther = new StringBuilder("\nOtherParams  : ");
      int pSize = allParamNames.size();
      String pName;
      if (pSize > 0) {

        for (int i = 0; i < pSize; i++) {
          pName = allParamNames.get(i);
          if (pathParamNames.contains(pName)) {
            sbPath.append(pName).append("(").append(allGenericParamTypes.get(i)).append(")  ");
          } else {
            sbOther.append(pName).append("(").append(allGenericParamTypes.get(i)).append(")  ");
          }
        }
      }

      sb.append(sbPath).append(sbOther);

      Type returnType = method.getGenericReturnType();
      sb.append("\nReturnType   : ").append(returnType);
      sb.append("\nDescriptions : ").append(des);

      if (validators != null && validators.length > 0 && validsLineNumbers != null) {
        sb.append("\nValidators   : ");
        int i = 0;
        for (Validator validator : validators) {
          if (i > 0)
            sb.append("\n               ");
          Class<? extends Validator> vc = validator.getClass();
          sb.append(vc.getName()).append("(").append(vc.getSimpleName()).append(".java:").append(validsLineNumbers[i][0]).append(")");
          i++;
        }
      }

      if (interceptors != null && interceptors.length > 0 && interceptorsLineNumbers != null) {
        sb.append("\nInterceptors : ");
        int i = 0;
        for (Interceptor interceptor : interceptors) {
          if (i > 0) {
            sb.append("\n               ");
          }
          Class<? extends Interceptor> ic = interceptor.getClass();
          sb.append(ic.getName()).append("(").append(ic.getSimpleName()).append(".java:").append(interceptorsLineNumbers[i][0]).append(")");
          i++;
        }
      }

      sb.append("\n--------------------------------------------------------------------------------\n");
      logger.info(sb.toString());
    }
  }

  /**
   * 匹配路由
   *
   * @param request  request对象
   * @param response response对象
   * @return route
   */
  public RouteMatch match(HttpRequest request, HttpResponse response) {
    if (!this.httpMethod.equals(request.getHttpMethod())) {
      return null;
    }
    String restPath = request.getRestPath();

    String extension = "";
    if (restPath.contains(".")) {
      int index = restPath.lastIndexOf(".");
      extension = restPath.substring(index + 1);
      if (RenderFactory.contains(extension)) {
        restPath = restPath.substring(0, index);
      } else {
        extension = "";
      }
    }

    Matcher m = pattern.matcher(restPath);
    if (!m.matches()) {
      return null;
    }
    if (headers.size() > 0) {
      for (Map.Entry<String, String> headersEntry : headers.entrySet()) {
        if (!headersEntry.getValue().equals(request.getHeader(headersEntry.getKey()))) {
          return null;
        }
      }
    }

    //pathParams
    Map<String, String> pathParams = new HashMap<String, String>();
    for (int i = 0; i < m.groupCount() && i < pathParamNames.size(); i++) {
      pathParams.put(pathParamNames.get(i), m.group(i + 1));
    }
    //formParams
    Map<String, List<String>> formParams = null;

    //有文件上传
    MultipartParam multipartParam = null;
    if (multipartBuilder != null) {
      multipartParam = multipartBuilder.readMultipart(request);
    }

    RouteMatch routeMatch = null;
    Params params = null;
    Map<String, UploadedFile> fileParams = null;
    String jsonParams = null;
    String contentType = request.getContentType();
    try {
      if (contentType != null && contentType.toLowerCase().contains(ContentType.JSON.value())) {
        //从 queryString 取json
        String queryString = request.getQueryString();

        if (queryString != null && (httpMethod.equals(HttpMethod.GET) || httpMethod.equals(HttpMethod.DELETE)) && Jsoner.isJson(queryString)) {
          jsonParams = queryString;

        } else {
          jsonParams = getJson(request);
          formParams = request.getQueryParams();
        }

        printMatchRoute(request.getContentType(), jsonParams, pathParams, formParams, null);
        params = parseJsonParams(jsonParams, pathParams, formParams);
      } else {
        formParams = request.getQueryParams();
        //print match route
        if (multipartParam != null) {
          fileParams = multipartParam.getUploadedFiles();

          if (formParams != null) {
            formParams.putAll(multipartParam.getParams());
          } else {
            formParams = multipartParam.getParams();
          }

          printMatchRoute(request.getContentType(), null, pathParams, formParams, fileParams);
          params = parseFormParams(pathParams, formParams, fileParams);
        } else {
          printMatchRoute(request.getContentType(), null, pathParams, formParams, null);
          params = parseFormParams(pathParams, formParams, new Hashtable<String, UploadedFile>());
        }
      }
    } catch (Exception e) {
      printMatchRoute(request.getContentType(), jsonParams, pathParams, formParams, fileParams);
      throwException(e);
    }
    routeMatch = new RouteMatch(pathPattern, restPath, extension, params, request, response);
    return routeMatch;
  }

  /**
   * 打印route信息
   *
   * @param pathParams path参数
   * @param formParams 其他参数
   * @param fileParams 文件参数
   */
  private void printMatchRoute(String contentType, String jsonParams, Map<String, String> pathParams, Map<String, List<String>> formParams, Map<String, UploadedFile> fileParams) {
    if (Constant.showRoute && logger.isInfoEnabled()) {
      //print route
      StringBuilder sb = new StringBuilder("\n\nMatch route ----------------- ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append(" ------------------------------");
      sb.append("\nResource     : ").append(resourceClass.getName()).append("(").append(resourceClass.getSimpleName()).append(".java:" + allLineNumbers[0] + ")");
      sb.append("\nMethod       : ").append(method.getName());
      sb.append("\nPathPattern  : ").append(httpMethod).append(" ").append(pathPattern);
      sb.append("\nContentType  : ").append(contentType);
      //print pathParams
      sb.append("\nPathParams   : ");
      if (pathParams != null && pathParams.size() > 0) {
        Set<Map.Entry<String, String>> paramsEntrySet = pathParams.entrySet();
        for (Map.Entry<String, String> paramsEntry : paramsEntrySet) {
          sb.append(paramsEntry.getKey()).append(" = {").append(paramsEntry.getValue()).append("}");
          sb.append("  ");
        }
      }
      //print formParams
      if (formParams != null && formParams.size() > 0) {
        sb.append("\nFormParams   : ");
        List<String> values;
        Set<Map.Entry<String, List<String>>> formParamsEntrySet = formParams.entrySet();
        for (Map.Entry<String, List<String>> formParamsEntry : formParamsEntrySet) {
          values = formParamsEntry.getValue();
          if (values.size() >= 1) {
            sb.append(formParamsEntry.getKey()).append(" = {").append(values.get(0)).append("}");
          }
          sb.append("  ");
        }
      }
      //print jsonParams
      if (jsonParams != null) {
        sb.append("\nJsonParams   : ").append(jsonParams);
      }

      //print fileParams
      if (fileParams != null && fileParams.size() > 0) {
        sb.append("\nFileParams   : ");
        UploadedFile value;
        Set<Map.Entry<String, UploadedFile>> fileParamsEntrySet = fileParams.entrySet();
        for (Map.Entry<String, UploadedFile> fileParamsEntry : fileParamsEntrySet) {
          value = fileParamsEntry.getValue();
          sb.append(fileParamsEntry.getKey()).append(" = {").append(value.getOriginalFileName()).append("(").append(value.getContentType()).append(")}");
          sb.append("  ");
        }
      }

      Type returnType = method.getGenericReturnType();
      sb.append("\nReturnType   : ").append(returnType);

      if (validators != null && validators.length > 0) {
        sb.append("\nValidators   : ");
        int i = 0;
        for (Validator validator : validators) {
          if (i > 0)
            sb.append("\n               ");
          Class<? extends Validator> vc = validator.getClass();
          sb.append(vc.getName()).append("(").append(vc.getSimpleName()).append(".java:").append(validsLineNumbers[i][0]).append(")");
          i++;
        }
      }

      if (interceptors != null && interceptors.length > 0) {
        sb.append("\nInterceptors : ");
        int i = 0;
        for (Interceptor interceptor : interceptors) {
          if (i > 0) {
            sb.append("\n               ");
          }
          Class<? extends Interceptor> ic = interceptor.getClass();
          sb.append(ic.getName()).append("(").append(ic.getSimpleName()).append(".java:").append(interceptorsLineNumbers[i][0]).append(")");
          i++;
        }
      }

      sb.append("\n--------------------------------------------------------------------------------\n");
      logger.info(sb.toString());
    }
  }

  public String toString() {
    return httpMethod + " " + pathPattern + " " + Joiner.on(",").join(allParamNames);
  }

  public Class<? extends Resource> getResourceClass() {
    return resourceClass;
  }

  public Method getMethod() {
    return method;
  }

  public String getHttpMethod() {
    return httpMethod;
  }

  public String getPattern() {
    return pattern.pattern();
  }

  public String getPathPattern() {
    return pathPattern;
  }

  public String getStdPathPattern() {
    return stdPathPattern;
  }

  public List<String> getPathParamNames() {
    return pathParamNames;
  }

  public List<String> getAllParamNames() {
    return allParamNames;
  }

  public List<Class<?>> getAllParamTypes() {
    return allParamTypes;
  }

  public List<Type> getAllGenericParamTypes() {
    return allGenericParamTypes;
  }

  public Interceptor[] getInterceptors() {
    return interceptors;
  }

  public Validator[] getValidators() {
    return validators;
  }

  public int[] getAllLineNumbers() {
    return allLineNumbers;
  }
  // here comes the path pattern parsing logic
  // the code is pretty ugly with lot of cross dependencies, I tried to keep it performant, correct, and maintainable
  // not sure those goals are all achieved though

  public MultipartBuilder getMultipartBuilder() {
    return multipartBuilder;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  /**
   * 抛出异常
   *
   * @param throwable
   */
  public void throwException(Throwable throwable) {
    if (throwable instanceof WebException) {
      throw (WebException) throwable;
    } else {
      WebException exception = getWebException(throwable, 0);
      Throwable cause = throwable.getCause();
      if (cause != null) {
        logger.error("Route method invoke error.", cause);
      } else {
        logger.error("Route method invoke error.", throwable);
      }
      throw exception;
    }
  }

  private WebException getWebException(Throwable throwable, int deep) {
    WebException result = null;

    String message = throwable.getMessage();
    if (message == null) {
      Throwable cause = throwable.getCause();
      if (cause != null) {
        if (cause instanceof WebException) {
          result = (WebException) cause;
        } else {
          message = cause.getMessage();
          if (message == null && !throwable.equals(cause) && deep < 100) {
            result = getWebException(cause, ++deep);
          }
        }
      }
    } else {
      if (throwable instanceof WebException) {
        result = (WebException) throwable;
      }
    }

    if (result == null) {
      result = new WebException(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
    return result;
  }

  /**
   * 获取json字符串
   *
   * @param request
   * @return
   */
  private String getJson(HttpRequest request) {
    String json = null;
    try {
      InputStream is = request.getContentStream();
      if (is != null) {
        json = StreamReader.readString(is);
      }
    } catch (IOException e) {
      String msg = "Could not read inputStream when contentType is '" + request.getContentType() + "'.";
      logger.error(msg, e);
      throw new WebException(msg);
    }
    return json;
  }

  /**
   * 获取所有的请求参数
   *
   * @return 所有参数
   */
  private Params parseFormParams(Map<String, String> pathParams, Map<String, List<String>> formParams, Map<String, UploadedFile> fileParams) throws IllegalAccessException, InstantiationException {
    Params params = new Params();
    int i = 0;
    Class paramType = null;
    List<String> valueArr = null;
    //判断范型类型
    Type[] typeArguments;

    Class keyTypeClass;
    Class valueTypeClass;
    for (String name : allParamNames) {
      paramType = allParamTypes.get(i);

      //path里的参数
      if (pathParamNames.contains(name)) {
        if (paramType == String.class) {
          params.set(name, pathParams.get(name));
        } else
          params.set(name, ModelDeserializer.parse(pathParams.get(name), paramType));
      } else {//其他参数
        if (paramType == UploadedFile.class) {
          params.set(name, fileParams.remove(name));
        } else if (paramType == Map.class) {
          typeArguments = ((ParameterizedType) allGenericParamTypes.get(i)).getActualTypeArguments();
          if (typeArguments.length >= 2) {
            keyTypeClass = (Class) typeArguments[0];
            valueTypeClass = (Class) typeArguments[1];
            if (keyTypeClass == String.class && valueTypeClass == UploadedFile.class) {
              params.set(name, fileParams);
            } else {
              valueArr = formParams.remove(name);
              params.set(name, parseString(paramType, valueArr));
            }
          } else {
            valueArr = formParams.remove(name);
            params.set(name, parseString(paramType, valueArr));
          }
        } else {
          valueArr = formParams.remove(name);
          params.set(name, parseString(paramType, valueArr));
        }
      }
      i++;
    }
    String name;
    if (formParams.size() > 0) {
      for (Map.Entry<String, List<String>> formEntry : formParams.entrySet()) {
        name = formEntry.getKey();
        if (!params.containsName(name)) {
          valueArr = formEntry.getValue();
          if (valueArr != null && valueArr.size() > 0) {
            params.set(name, valueArr.get(0));
          } else {
            params.set(name, null);
          }
        }
      }
    }
    if (fileParams.size() > 0) {
      for (Map.Entry<String, UploadedFile> fileEntry : fileParams.entrySet()) {
        name = fileEntry.getKey();
        if (!params.containsName(name)) {
          params.set(fileEntry.getKey(), fileEntry.getValue());
        }
      }
    }
    return params;
  }


  /**
   * 转换string类型参数
   *
   * @param paramType
   * @param valueArr
   */
  private Object parseString(Class paramType, List<String> valueArr) throws InstantiationException, IllegalAccessException {
    String value;
    Object result = null;
    if (valueArr != null && valueArr.size() > 0) {
      //不支持数组参数
      value = valueArr.get(0);
      if (paramType == String.class) {
        result = value;
      } else {
        //转换为对应的对象类型
        result = ModelDeserializer.parse(value, paramType);
      }
    }
    return result;
  }


  /**
   * 获取所有以application/json方式提交的数据
   *
   * @param json json字符串
   * @return 所有参数
   */
  private Params parseJsonParams(String json, Map<String, String> pathParams, Map<String, List<String>> formParams) throws IllegalAccessException, InstantiationException {
    Params params = new Params();

    int i = 0;
    Class paramType = null;

    //只有一个参数时 直接把该参数 放入方法
    boolean oneParamParse = false;
    int paramSize = allParamNames.size() - pathParamNames.size();
    if (Constant.oneParamParse) {
      oneParamParse = paramSize == 1;
    }

    Object obj = null;
    List<String> valueArr = null;

    boolean hasJsonParam = null != json && !"".equals(json);
    Object receiveParams = null;
    if (hasJsonParam && !oneParamParse) {
      if (json.startsWith("\"") || json.startsWith("{") || json.startsWith("[")) {
        receiveParams = Jsoner.toObject(json);
      } else {
        receiveParams = Jsoner.toObject("\"" + json + "\"");
      }
      hasJsonParam = receiveParams != null;
    }
    for (String name : allParamNames) {
      paramType = allParamTypes.get(i);

      //path里的参数
      if (pathParamNames.contains(name)) {
        if (paramType == String.class) {
          params.set(name, pathParams.get(name));
        } else {
          params.set(name, Jsoner.toObject(pathParams.get(name), paramType));
        }
      } else {//其他参数
        if (hasJsonParam) {
          if (oneParamParse) {
            //转换对象到指定的类型
            params.set(name, parse(allGenericParamTypes.get(i), paramType, ModelDeserializer.parse(json, paramType)));
          } else {
            if (receiveParams instanceof Map) {
              obj = ((Map<String, Object>) receiveParams).remove(name);

              if (obj != null) {
                if (paramType == String.class) {
                  params.set(name, obj.toString());
                } else {
                  //转换对象到指定的类型
                  params.set(name, parse(allGenericParamTypes.get(i), paramType, obj));
                }
              }
            }
          }
        }
        //没有获取到的参数设置为空
        if (!params.containsName(name)) {
          if (formParams != null && formParams.size() > 0) {
            valueArr = formParams.remove(name);
          }
          params.set(name, parseString(paramType, valueArr));
        }
      }

      i++;
    }

    String name;
    if (hasJsonParam && receiveParams instanceof Map && !oneParamParse) {
      for (Map.Entry<String, Object> receiveEntry : ((Map<String, Object>) receiveParams).entrySet()) {
        name = receiveEntry.getKey();
        if (!params.containsName(name)) {
          params.set(receiveEntry.getKey(), receiveEntry.getValue());
        }
      }
    }

    if (formParams != null && formParams.size() > 0) {
      for (Map.Entry<String, List<String>> formEntry : formParams.entrySet()) {
        name = formEntry.getKey();
        if (!params.containsName(name)) {
          valueArr = formEntry.getValue();
          if (valueArr != null && valueArr.size() > 0) {
            params.set(name, valueArr.get(0));
          } else {
            params.set(name, null);
          }
        }
      }
    }
    return params;
  }

  /**
   * 把参数转到对应的类型
   *
   * @param paramType
   * @param obj
   */
  private Object parse(Type genericParamType, Class paramType, Object obj) throws InstantiationException, IllegalAccessException {
    Class keyTypeClass;
    Class paramTypeClass;
    List<JSONObject> list;
    List<Entity> newlist;
    List blist;
    List newblist;

    Set<JSONObject> set;
    Set<Entity> newset;
    Set bset;
    Set newbset;
    Map map;
    Set<Map.Entry<String, Object>> mapEntry;
    Map newMap;

    Object result = null;
    if (obj != null) {
      if (Map.class.isAssignableFrom(paramType)) {
        keyTypeClass = (Class) ((ParameterizedType) genericParamType).getActualTypeArguments()[0];
        paramTypeClass = (Class) ((ParameterizedType) genericParamType).getActualTypeArguments()[1];
        map = (Map<String, Object>) obj;
        newMap = new HashMap();
        mapEntry = map.entrySet();
        for (Map.Entry<String, Object> entry : mapEntry) {
          newMap.put(ModelDeserializer.parse(entry.getKey(), keyTypeClass), ModelDeserializer.parse(entry.getValue(), paramTypeClass));
        }
        result = newMap;
      } else if (Collection.class.isAssignableFrom(paramType)) {
        paramTypeClass = (Class) ((ParameterizedType) genericParamType).getActualTypeArguments()[0];

        if (List.class.isAssignableFrom(paramType)) {
          //Entity类型
          if (Entity.class.isAssignableFrom(paramTypeClass)) {
            list = (List<JSONObject>) obj;
            newlist = new ArrayList<Entity>();
            for (JSONObject jo : list) {
              newlist.add(ModelDeserializer.deserialze(jo, paramTypeClass));
            }
            result = newlist;
          } else {
            blist = (List) obj;
            if (String.class == paramTypeClass) {
              newblist = new ArrayList<String>();
              for (Object o : blist) {
                newblist.add(o.toString());
              }
            } else {
              newblist = new ArrayList();
              for (Object o : blist) {
                if (paramTypeClass.isAssignableFrom(o.getClass())) {
                  newblist.add(o);
                } else {
                  newblist.add(ModelDeserializer.parse(o, paramTypeClass));
                }
              }
            }
            result = newblist;
          }
        } else if (Set.class.isAssignableFrom(paramType)) {
          //Entity
          if (Entity.class.isAssignableFrom(paramTypeClass)) {
            set = (Set<JSONObject>) obj;
            newset = new HashSet<Entity>();
            for (JSONObject jo : set) {
              newset.add(ModelDeserializer.deserialze(jo, paramTypeClass));
            }
            result = newset;
          } else {
            bset = (Set) obj;
            if (String.class == paramTypeClass) {
              newbset = new HashSet<String>();
              for (Object o : bset) {
                newbset.add(o.toString());
              }
            } else {
              newbset = new HashSet();
              for (Object o : bset) {
                if (paramTypeClass.isAssignableFrom(o.getClass())) {
                  newbset.add(o);
                } else {
                  newbset.add(ModelDeserializer.parse(o, paramTypeClass));
                }
              }
            }
            result = newbset;
          }
        }
      } else {
        result = ModelDeserializer.parse(obj, paramType);
      }
    }
    return result;
  }


  private static interface PathParserCharProcessor {
    void handle(int curChar, PathPatternParser pathPatternParser);

    void end(PathPatternParser pathPatternParser);
  }

  private static final class PathPatternParser {
    final int length;
    final String pathPattern;
    int offset = 0;
    PathParserCharProcessor processor = regularCharPathParserCharProcessor;
    List<String> pathParamNames = new ArrayList<String>();
    StringBuilder patternBuilder = new StringBuilder();
    StringBuilder stdPathPatternBuilder = new StringBuilder();

    private PathPatternParser(String pathPattern) {
      this.length = pathPattern.length();
      this.pathPattern = pathPattern;
    }

    void parse() {
      while (offset < length) {
        int curChar = pathPattern.codePointAt(offset);

        processor.handle(curChar, this);

        offset += Character.charCount(curChar);
      }
      processor.end(this);
    }
  }

  private static final class CurlyBracesPathParamPathParserCharProcessor implements PathParserCharProcessor {
    private int openBr = 1;
    private boolean inRegexDef;
    private StringBuilder pathParamName = new StringBuilder();
    private StringBuilder pathParamRegex = new StringBuilder();


    public void handle(int curChar, PathPatternParser pathPatternParser) {
      if (curChar == '}') {
        openBr--;
        if (openBr == 0) {
          // found matching brace, end of path param

          if (pathParamName.length() == 0) {
            // it was a mere {}, can't be interpreted as a path param
            pathPatternParser.processor = regularCharPathParserCharProcessor;
            pathPatternParser.patternBuilder.append("{}");
            pathPatternParser.stdPathPatternBuilder.append("{}");
            return;
          }

          // only the opening paren
          checkArgument(pathParamRegex.length() != 1, "illegal path parameter definition '%s' at offset %d - custom regex must not be empty",
              pathPatternParser.pathPattern, pathPatternParser.offset);


          if (pathParamRegex.length() == 0) {
            // use default regex
            pathParamRegex.append(PARAM_PATTERN);
          } else {
            // close paren for matching group
            pathParamRegex.append(")");
          }

          pathPatternParser.processor = regularCharPathParserCharProcessor;
          pathPatternParser.patternBuilder.append(pathParamRegex);
          pathPatternParser.stdPathPatternBuilder.append("{").append(pathParamName).append("}");
          pathPatternParser.pathParamNames.add(pathParamName.toString());
          return;
        }
      } else if (curChar == '{') {
        openBr++;
      }

      if (inRegexDef) {
        pathParamRegex.appendCodePoint(curChar);
      } else {
        if (curChar == ':') {
          // we were in path name, the column marks the separator with the regex definition, we go in regex mode
          inRegexDef = true;
          pathParamRegex.append("(");
        } else {

          //only letters are authorized in path param name
          checkArgument(Character.isLetterOrDigit(curChar), "illegal path parameter definition '%s' at offset %d" +
                  " - only letters and digits are authorized in path param name",
              pathPatternParser.pathPattern, pathPatternParser.offset);

          pathParamName.appendCodePoint(curChar);
        }
      }
    }


    public void end(PathPatternParser pathPatternParser) {
    }
  }

  private static final class SimpleColumnBasedPathParamParserCharProcessor implements PathParserCharProcessor {
    private StringBuilder pathParamName = new StringBuilder();

    public void handle(int curChar, PathPatternParser pathPatternParser) {
//      if (!Character.isLetterOrDigit(curChar) && curChar != '_') {
      if (curChar == '/') {
        pathPatternParser.patternBuilder.append(PARAM_PATTERN);
        pathPatternParser.stdPathPatternBuilder.append("{").append(pathParamName).append("}");
        pathPatternParser.pathParamNames.add(pathParamName.toString());
        pathPatternParser.processor = regularCharPathParserCharProcessor;
        pathPatternParser.processor.handle(curChar, pathPatternParser);
      } else {
        pathParamName.appendCodePoint(curChar);
      }
    }


    public void end(PathPatternParser pathPatternParser) {
      pathPatternParser.patternBuilder.append(PARAM_PATTERN);
      pathPatternParser.stdPathPatternBuilder.append("{").append(pathParamName).append("}");
      pathPatternParser.pathParamNames.add(pathParamName.toString());
    }
  }

}
