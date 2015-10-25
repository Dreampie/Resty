package cn.dreampie.common.spring;

import cn.dreampie.common.util.Stringer;
import cn.dreampie.log.Logger;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;

import static cn.dreampie.common.util.Checker.checkNotNull;

/**
 * @author Dreampie
 * @date 2015-10-08
 * @what
 */
public class SpringBuilder {

  private final static Logger logger = Logger.getLogger(SpringBuilder.class);

  private static ConfigurableApplicationContext context;

  public static ConfigurableApplicationContext getContext() {
    return SpringBuilder.context;
  }

  public static void setContext(ConfigurableApplicationContext context) {
    checkNotNull(context, "Could not found context for spring.");
    SpringBuilder.context = context;
    SpringHolder.alive = true;
  }

  public static void refreshContext() {
    if (SpringHolder.alive) {
      SpringBuilder.context.refresh();
    }
  }

  public static void removeContext() {
    if (SpringHolder.alive) {
      SpringBuilder.context.close();
      SpringBuilder.context = null;
      SpringHolder.alive = false;
    }
  }

  /**
   * 注册bean
   *
   * @param clazz
   */
  public static void register(Class clazz) {
    ConfigurableApplicationContext context = getContext();
    if (context != null) {
      DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) context.getBeanFactory();
      String beanName = Stringer.firstLowerCase(clazz.getSimpleName());
      beanFactory.registerBeanDefinition(beanName, BeanDefinitionBuilder.rootBeanDefinition(clazz).getBeanDefinition());
    }
  }

  public static void registerSingleton(Class clazz) {
    try {
      registerSingleton(clazz, clazz.newInstance());
    } catch (InstantiationException e) {
      logger.error(e.getMessage(), e);
    } catch (IllegalAccessException e) {
      logger.error(e.getMessage(), e);
    }
  }

  public static void registerSingleton(Class clazz, Object bean) {
    ConfigurableApplicationContext context = getContext();
    if (context != null) {
      DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) context.getBeanFactory();
      String beanName = Stringer.firstLowerCase(clazz.getSimpleName());
      beanFactory.registerSingleton(beanName, bean);
    }
  }

  public static <T> T getBean(Class<T> clazz) {
    ConfigurableApplicationContext context = getContext();
    if (context != null) {
      return context.getBean(clazz);
    }
    return null;
  }
}
