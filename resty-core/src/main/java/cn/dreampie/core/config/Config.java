package cn.dreampie.core.config;


/**
 * Config.
 * <p/>
 * Config order: configConstant(), configResource(), configPlugin(), configInterceptor(), configHandler()
 */
public abstract class Config {

  /**
   * Config constant
   */
  public abstract void configConstant(ConstantLoader constantLoader);

  /**
   * Config resource
   */
  public abstract void configResource(ResourceLoader resourceLoader);

  /**
   * Config plugin
   */
  public abstract void configPlugin(PluginLoader pluginLoader);

  /**
   * Config interceptor applied to all actions.
   */
  public abstract void configInterceptor(InterceptorLoader interceptorLoader);

  /**
   * Config handler
   */
  public abstract void configHandler(HandlerLoader handlerLoader);


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