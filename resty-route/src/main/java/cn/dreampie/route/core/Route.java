package cn.dreampie.route.core;


import cn.dreampie.common.Constant;
import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.common.http.UploadedFile;
import cn.dreampie.common.util.Joiner;
import cn.dreampie.common.util.analysis.ParamAttribute;
import cn.dreampie.common.util.analysis.ParamNamesScaner;
import cn.dreampie.log.Logger;
import cn.dreampie.route.core.multipart.MultipartBuilder;
import cn.dreampie.route.core.multipart.MultipartParam;
import cn.dreampie.route.exception.InitException;
import cn.dreampie.route.interceptor.Interceptor;
import cn.dreampie.route.render.RenderFactory;
import cn.dreampie.route.valid.Validator;

import java.lang.reflect.Method;
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

  public static final String DEFAULT_PATTERN = "([^\\/]+)";
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


  public Route(Class<? extends Resource> resourceClass, ParamAttribute paramAttribute, String httpMethod, String pathPattern, Method method, Interceptor[] interceptors, String des, Validator[] validators, MultipartBuilder multipartBuilder) {
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
    Matcher m = pattern.matcher(restPath);
    if (!m.matches()) {
      return null;
    }

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
    //pathParams
    Map<String, String> pathParams = new HashMap<String, String>();
    for (int i = 0; i < m.groupCount() && i < pathParamNames.size(); i++) {
      pathParams.put(pathParamNames.get(i), m.group(i + 1));
    }
    //otherParams
    Map<String, List<String>> otherParams = request.getQueryParams();

    //有文件上传
    MultipartParam multipartParam = null;
    if (multipartBuilder != null) {
      multipartParam = multipartBuilder.readMultipart(request);
    }

    RouteMatch routeMatch = null;
    //print match route
    if (multipartParam != null) {
      otherParams.putAll(multipartParam.getParams());
      printMatchRoute(pathParams, otherParams, multipartParam.getUploadedFiles());
      routeMatch = new RouteMatch(pathPattern, restPath, extension, pathParams, otherParams, multipartParam.getUploadedFiles(), request, response);
    } else {
      printMatchRoute(pathParams, otherParams, null);
      routeMatch = new RouteMatch(pathPattern, restPath, extension, pathParams, otherParams, null, request, response);
    }
    return routeMatch;
  }

  /**
   * 打印route信息
   *
   * @param pathParams  path参数
   * @param otherParams 其他参数
   * @param fileParams  文件参数
   */
  private void printMatchRoute(Map<String, String> pathParams, Map<String, List<String>> otherParams, Map<String, UploadedFile> fileParams) {
    if (Constant.showRoute && logger.isInfoEnabled()) {
      //print route
      StringBuilder sb = new StringBuilder("\n\nMatch route ----------------- ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append(" ------------------------------");
      sb.append("\nResource     : ").append(resourceClass.getName()).append("(").append(resourceClass.getSimpleName()).append(".java:" + allLineNumbers[0] + ")");
      sb.append("\nMethod       : ").append(method.getName());
      sb.append("\nPathPattern  : ").append(httpMethod).append(" ").append(pathPattern);
      //print pathParams
      sb.append("\nPathParams   : ");
      if (pathParams.size() > 0) {
        Set<Map.Entry<String, String>> paramsEntrySet = pathParams.entrySet();
        for (Map.Entry<String, String> paramsEntry : paramsEntrySet) {
          sb.append(paramsEntry.getKey()).append(" = {").append(paramsEntry.getValue()).append("}");
          sb.append("  ");
        }
      }
      //print otherParams
      if (otherParams != null) {
        sb.append("\nOtherParams  : ");
        List<String> values;
        Set<Map.Entry<String, List<String>>> otherParamsEntrySet = otherParams.entrySet();
        for (Map.Entry<String, List<String>> otherParamsEntry : otherParamsEntrySet) {
          values = otherParamsEntry.getValue();
          if (values.size() >= 1) {
            sb.append(otherParamsEntry.getKey()).append(" = {").append(values.get(0)).append("}");
          }
          sb.append("  ");
        }
      }

      //print fileParams
      if (fileParams != null) {
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

  // here comes the path pattern parsing logic
  // the code is pretty ugly with lot of cross dependencies, I tried to keep it performant, correct, and maintainable
  // not sure those goals are all achieved though

  public MultipartBuilder getMultipartBuilder() {
    return multipartBuilder;
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
            pathParamRegex.append(DEFAULT_PATTERN);
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
        pathPatternParser.patternBuilder.append(DEFAULT_PATTERN);
        pathPatternParser.stdPathPatternBuilder.append("{").append(pathParamName).append("}");
        pathPatternParser.pathParamNames.add(pathParamName.toString());
        pathPatternParser.processor = regularCharPathParserCharProcessor;
        pathPatternParser.processor.handle(curChar, pathPatternParser);
      } else {
        pathParamName.appendCodePoint(curChar);
      }
    }


    public void end(PathPatternParser pathPatternParser) {
      pathPatternParser.patternBuilder.append(DEFAULT_PATTERN);
      pathPatternParser.stdPathPatternBuilder.append("{").append(pathParamName).append("}");
      pathPatternParser.pathParamNames.add(pathParamName.toString());
    }
  }

}
