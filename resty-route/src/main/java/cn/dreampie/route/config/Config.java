package cn.dreampie.route.config;


/**
 * Config.
 * <p/>
 * Config order: configConstant(), configResource(), configPlugin(), configInterceptor(), configHandler()
 */
public class Config {

  /**
   * Config constant
   */
  public void configConstant(ConstantLoader constantLoader) {
  }

  /**
   * Config resource
   */
  public void configResource(ResourceLoader resourceLoader) {
  }

  /**
   * Config plugin
   */
  public void configPlugin(PluginLoader pluginLoader) {
  }

  /**
   * Config interceptor applied to all actions.
   */
  public void configInterceptor(InterceptorLoader interceptorLoader) {
  }

  /**
   * Config handler
   */
  public void configHandler(HandlerLoader handlerLoader) {
  }


  /**
   * Call back after Resty start
   */
  public void afterStart() {
  }

  /**
   * Call back before Resty stop
   */
  public void beforeStop() {
  }

}