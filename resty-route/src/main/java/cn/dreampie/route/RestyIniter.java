package cn.dreampie.route;

import cn.dreampie.route.config.Config;
import cn.dreampie.route.core.RouteBuilder;
import cn.dreampie.route.core.RouteHandler;
import cn.dreampie.route.handler.Handler;
import cn.dreampie.route.handler.HandlerFactory;

import javax.servlet.ServletContext;

/**
 * Resty
 */
public final class RestyIniter {

  private ConfigIniter configIniter;
  private Handler handler;
  private ServletContext servletContext;
  private Config config;

  public RestyIniter(Config config, ServletContext servletContext) {
    this.servletContext = servletContext;
    this.config = config;
    configIniter = new ConfigIniter(config);
    //build route
    RouteBuilder routeBuilder = new RouteBuilder(configIniter.getResourceLoader(), configIniter.getInterceptorLoader());
    routeBuilder.build();
    //add handler
    //must after route build
    Handler routeHandler = new RouteHandler(routeBuilder);
    handler = HandlerFactory.getHandler(configIniter.getHandlerLoader().getHandlerList(), routeHandler);
    //start job when config over
    config.afterStart();
  }

  public void stop() {
    config.beforeStop();
    configIniter.stopPlugins();
  }


  public ServletContext getServletContext() {
    return servletContext;
  }

  public Handler getHandler() {
    return handler;
  }
}










