package cn.dreampie.route;

import cn.dreampie.common.Constant;
import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
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

  private Handler handler;
  private String encoding = Constant.encoding;
  private Config config;
  public static final String PARAM_NAME_CONFIGCLASS = "configClass";
  public static final String PARAM_NAME_EXCLUSIONS = "exclusions";
  private Set<String> excludesPattern;

  private static final RestyIniter restyIniter = RestyIniter.instance();
  private static final Logger logger = Logger.getLogger(RestyFilter.class);

  public void init(FilterConfig filterConfig) throws ServletException {
    {
      String exclusions = filterConfig.getInitParameter(PARAM_NAME_EXCLUSIONS);
      if (exclusions != null && exclusions.trim().length() != 0) {
        excludesPattern = new HashSet<String>(Arrays.asList(exclusions.split("\\s*,\\s*")));
      }
    }
    createConfig(filterConfig.getInitParameter(PARAM_NAME_CONFIGCLASS));

    if (!restyIniter.init(config, filterConfig.getServletContext()))
      throw new InitException("Resty init error!");

    handler = restyIniter.getHandler();
  }

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

    HttpRequest request = new HttpRequest((HttpServletRequest) servletRequest);
    HttpResponse response = new HttpResponse((HttpServletResponse) servletResponse, (HttpServletRequest) servletRequest);
    request.setCharacterEncoding(encoding);


    boolean[] isHandled = {false};
    //排除的参数
    if (!isExclusion(request.getRestUri())) {

      try {
        handler.handle(request, response, isHandled);
      } catch (WebException e) {
        response.setStatus(e.getStatus());
        RenderFactory.getByUrl(request.getRestPath()).render(request, response, e.getContent());
        if (logger.isErrorEnabled()) {
          logger.warn("Request \"" + request.getHttpMethod() + " " + request.getRestPath() + "\" error - " + e.getMessage());
        }
      } catch (Exception e) {
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
    config.beforeStop();
    restyIniter.stopPlugins();
  }

  private void createConfig(String configClass) {
    if (configClass != null) {
      Object temp = null;
      try {
        temp = Class.forName(configClass).newInstance();
      } catch (Exception e) {
        throw new InitException("Could not create instance of class: " + configClass, e);
      }

      if (temp instanceof Config)
        config = (Config) temp;
      else
        throw new InitException("Could not create instance of class: " + configClass + ". Please check the config in web.xml");
    } else {
      config = new NoConfig();
      logger.warn("Could not found config and start in NoConfig.");
    }

  }
}
