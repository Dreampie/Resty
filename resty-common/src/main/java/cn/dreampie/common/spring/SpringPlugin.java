package cn.dreampie.common.spring;

import cn.dreampie.common.Plugin;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Dreampie
 * @date 2015-10-08
 * @what
 */
public class SpringPlugin implements Plugin {

  private String[] configFiles;
  private Class[] configClasses;
  private ConfigurableApplicationContext context;

  /**
   * Use configuration under the path of WebRoot/WEB-INF.
   */
  public SpringPlugin() {
    this.configFiles = new String[]{"classpath:applicationContext.xml"};
  }

  public SpringPlugin(String... configFiles) {
    this.configFiles = configFiles;
  }

  public SpringPlugin(ConfigurableApplicationContext context) {
    this.context = context;
  }

  public SpringPlugin(Class... configClasses) {
    this.configClasses = configClasses;
  }

  public boolean start() {
    if (this.context == null) {
      if (configFiles == null) {
        this.context = new AnnotationConfigApplicationContext(configClasses);
      } else {
        this.context = new ClassPathXmlApplicationContext(configFiles);
      }
    }
    SpringBuilder.setContext(context);
    return true;
  }

  public boolean stop() {
    SpringBuilder.removeContext();
    return true;
  }
}
