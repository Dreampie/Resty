package cn.dreampie.route;

import cn.dreampie.common.Constant;
import cn.dreampie.route.config.Config;
import cn.dreampie.route.handler.Handler;
import cn.dreampie.route.http.HttpRequest;
import cn.dreampie.route.http.HttpResponse;
import cn.dreampie.log.Logger;
import cn.dreampie.log.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Resty framework filter
 */
public final class RestyFilter implements Filter {

  private Handler handler;
  private String encoding = Constant.encoding;
  private Config config;
  private static final RestyIniter RESTY_INITER = RestyIniter.instance();
  private static final Logger logger = LoggerFactory.getLogger(RestyFilter.class);

  public void init(FilterConfig filterConfig) throws ServletException {
    createConfig(filterConfig.getInitParameter("configClass"));

    if (!RESTY_INITER.init(config, filterConfig.getServletContext()))
      throw new RuntimeException("Resty init error!");

    handler = RESTY_INITER.getHandler();

  }

  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
    if (!(servletRequest instanceof HttpServletRequest)
        || !(servletResponse instanceof HttpServletResponse)) {
      throw new ServletException("Resty doesn't support non-HTTP request or response.");
    }

    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;
    request.setCharacterEncoding(encoding);

    boolean[] isHandled = {false};
    try {
      handler.handle(new HttpRequest(request), new HttpResponse(response, request), isHandled);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    if (!isHandled[0])
      chain.doFilter(request, response);
  }

  public void destroy() {
    config.beforeStop();
    RESTY_INITER.stopPlugins();
  }

  private void createConfig(String configClass) {
    if (configClass != null) {
      Object temp = null;
      try {
        temp = Class.forName(configClass).newInstance();
      } catch (Exception e) {
        throw new RuntimeException("Can not create instance of class: " + configClass, e);
      }

      if (temp instanceof Config)
        config = (Config) temp;
      else
        throw new RuntimeException("Can not create instance of class: " + configClass + ". Please check the config in web.xml");
    } else {
      config = new NoConfig();
      logger.warn("Can not found config and start in NoConfig.");
    }

  }
}
