package cn.dreampie.route;

import cn.dreampie.common.Constant;
import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.common.http.HttpStatus;
import cn.dreampie.common.http.exception.WebException;
import cn.dreampie.common.util.pattern.AntPathMatcher;
import cn.dreampie.log.Logger;
import cn.dreampie.route.config.Config;
import cn.dreampie.route.exception.InitException;
import cn.dreampie.route.handler.Handler;
import cn.dreampie.route.render.RenderFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Resty framework filter
 */
public final class RestyFilter implements Filter {

  private static final Logger logger = Logger.getLogger(RestyFilter.class);

  private RestyIniter restyIniter;
  private Handler handler;
  private String encoding = Constant.encoding;
  public static final String PARAM_NAME_CONFIGCLASS = "configClass";
  public static final String PARAM_NAME_EXCLUSIONS = "exclusions";
  private Set<String> excludesPattern;

  public void init(FilterConfig filterConfig) throws ServletException {
    {
      String exclusions = filterConfig.getInitParameter(PARAM_NAME_EXCLUSIONS);
      if (exclusions != null && exclusions.trim().length() != 0) {
        excludesPattern = new HashSet<String>(Arrays.asList(exclusions.split("\\s*,\\s*")));
      }
    }
    try {
      Config config = createConfig(filterConfig.getInitParameter(PARAM_NAME_CONFIGCLASS));
      restyIniter = new RestyIniter(config, filterConfig.getServletContext());
      handler = restyIniter.getHandler();
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  /**
   * 判断是否是需要排除的请求
   *
   * @param requestURI 请求uri
   * @return boolean
   */
  public boolean isExclusion(String requestURI) {
    if (excludesPattern == null) {
      return false;
    }
    for (String pattern : excludesPattern) {
      if (AntPathMatcher.instance().matches(pattern, requestURI)) {
        return true;
      }
    }
    return false;
  }

  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
    if (!(servletRequest instanceof HttpServletRequest)
        || !(servletResponse instanceof HttpServletResponse)) {
      throw new ServletException("Resty doesn't support non-HTTP request or response.");
    }
    servletRequest.setCharacterEncoding(encoding);
    HttpRequest request = new HttpRequest((HttpServletRequest) servletRequest);
    HttpResponse response = new HttpResponse((HttpServletResponse) servletResponse, (HttpServletRequest) servletRequest);

    boolean[] isHandled = {false};
    //排除的参数
    if (!isExclusion(request.getRestPath())) {
      try {
        handler.handle(request, response, isHandled);
      } catch (WebException e) {
        response.setStatus(e.getStatus());
        RenderFactory.getByUrl(request.getRestPath()).render(request, response, e.getContent());
        if (logger.isErrorEnabled()) {
          logger.warn("Request \"" + request.getHttpMethod() + " " + request.getRestPath() + "\" error - " + e.getMessage());
        }
      } catch (Exception e) {
        response.setStatus(HttpStatus.BAD_REQUEST);
        RenderFactory.getByUrl(request.getRestPath()).render(request, response, e.getMessage());
        if (logger.isErrorEnabled()) {
          logger.error(request.getRestPath(), e);
        }
      }
    }
    if (!isHandled[0])
      chain.doFilter(servletRequest, servletResponse);
  }

  public void destroy() {
    try {
      restyIniter.stop();
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  /**
   * 读取配置类
   *
   * @param configClass class地址
   * @return Config
   */
  private Config createConfig(String configClass) {
    Config config = null;
    if (configClass != null) {
      Object temp = null;
      try {
        temp = Class.forName(configClass).newInstance();
      } catch (Exception e) {
        throw new InitException("Could not create instance of class: " + configClass, e);
      }
      if (temp instanceof Config) {
        config = (Config) temp;
      } else {
        throw new InitException("Could not create instance of class: " + configClass + ". Please check the init in web.xml");
      }
    } else {
      config = new Config();
      logger.warn("Could not found init and start in no init.");
    }
    return config;
  }
}
