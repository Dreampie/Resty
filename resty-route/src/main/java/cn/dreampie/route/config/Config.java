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
  public void configConstant(Constants constants) {
  }

  /**
   * Config resource
   */
  public void configResource(Resources resources) {
  }
  
  /**
   * Config plugin
   */
  public void configPlugin(Plugins plugins) {
  }

  /**
   * Config interceptor applied to all actions.
   */
  public void configInterceptor(Interceptors interceptors) {
  }

  /**
   * Config handler
   */
  public void configHandler(Handlers handlers) {
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