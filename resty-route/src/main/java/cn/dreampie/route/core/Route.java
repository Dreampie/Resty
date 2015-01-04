package cn.dreampie.route.core;


import cn.dreampie.log.Logger;
import cn.dreampie.log.LoggerFactory;
import cn.dreampie.route.base.Render;
import cn.dreampie.route.base.Request;
import cn.dreampie.route.core.base.Resource;
import cn.dreampie.route.interceptor.Interceptor;
import cn.dreampie.route.render.FileRender;
import cn.dreampie.route.render.JsonRender;
import cn.dreampie.route.render.RenderFactory;
import cn.dreampie.util.Joiner;
import cn.dreampie.util.ParamNamesScaner;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.dreampie.util.Checker.checkArgument;
import static cn.dreampie.util.Checker.checkNotNull;

/**
 * Created by ice on 14-12-19.
 */
public class Route {

  private static final Logger logger = LoggerFactory.getLogger(Route.class);
  private final String httpMethod;
  private final String pathPattern;
  private final String stdPathPattern;

  private final Pattern pattern;
  private final List<String> pathParamNames;

  private final Class<? extends Resource> resourceClass;
  private final Method method;
  private final List<String> allParamNames;
  private final List<Class<?>> allParamTypes;

  private final Interceptor[] interceptors;

  private final Render render;

  public Route(Class<? extends Resource> resourceClass, String httpMethod, String pathPattern, Method method, Interceptor[] interceptors) {
    this.resourceClass = resourceClass;
    this.httpMethod = checkNotNull(httpMethod);
    this.pathPattern = checkNotNull(pathPattern);
    this.method = method;
    //文件类型
    if (File.class.isAssignableFrom(method.getReturnType())) {
      render = new FileRender();
    } else {
      render = null;
    }

    this.interceptors = interceptors;

    allParamNames = ParamNamesScaner.getParamNames(method);
    allParamTypes = Arrays.asList(method.getParameterTypes());


    PathPatternParser s = new PathPatternParser(pathPattern);
    s.parse();

    pattern = Pattern.compile(s.patternBuilder.toString());
    stdPathPattern = s.stdPathPatternBuilder.toString();
    pathParamNames = s.pathParamNames;

    logger.info("Route build " + httpMethod + " " + pathPattern + " -> " + pattern);
  }


  public RouteMatch match(Request request) {
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

    Map<String, String> params = new HashMap<String, String>();
    for (int i = 0; i < m.groupCount() && i < pathParamNames.size(); i++) {
      params.put(pathParamNames.get(i), m.group(i + 1));
    }

    if (render != null) {
      return new RouteMatch(pathPattern, restPath, render, params, request.getQueryParams());
    } else {
      return new RouteMatch(pathPattern, restPath, extension, params, request.getQueryParams());
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

  public Interceptor[] getInterceptors() {
    return interceptors;
  }

  // here comes the path pattern parsing logic
  // the code is pretty ugly with lot of cross dependencies, I tried to keep it performant, correct, and maintainable
  // not sure those goals are all achieved though

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

  private static interface PathParserCharProcessor {
    void handle(int curChar, PathPatternParser pathPatternParser);

    void end(PathPatternParser pathPatternParser);
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
            pathParamRegex.append("([^\\/]+)");
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
      if (!Character.isLetterOrDigit(curChar)) {
        pathPatternParser.patternBuilder.append("([^\\/]+)");
        pathPatternParser.stdPathPatternBuilder.append("{").append(pathParamName).append("}");
        pathPatternParser.pathParamNames.add(pathParamName.toString());
        pathPatternParser.processor = regularCharPathParserCharProcessor;
        pathPatternParser.processor.handle(curChar, pathPatternParser);
      } else {
        pathParamName.appendCodePoint(curChar);
      }
    }


    public void end(PathPatternParser pathPatternParser) {
      pathPatternParser.patternBuilder.append("([^\\/]+)");
      pathPatternParser.stdPathPatternBuilder.append("{").append(pathParamName).append("}");
      pathPatternParser.pathParamNames.add(pathParamName.toString());
    }
  }

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
}
