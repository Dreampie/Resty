package cn.dreampie.common.ioc;

import cn.dreampie.log.Logger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dreampie
 * @date 2015-06-12
 * @what
 */
public class ApplicationContainer implements Serializable {
  private static final Logger logger = Logger.getLogger(ApplicationContainer.class);
  //bean 存储容器  name,bean
  private static Map<String, BeanContainer> beanContainers = new HashMap<String, BeanContainer>();

  //class,name
  private static Map<Class<?>, String> beanClasses = new HashMap<Class<?>, String>();

  public static <T> T get(String name) {
    return beanContainers.get(name).get();
  }

  public static <T> T get(Class<?> clazz) {
    return get(beanClasses.get(clazz));
  }

  public static void set(Class<?> clazz) {
    BeanContainer beanContainer = new BeanContainer(clazz);
    beanClasses.put(clazz, beanContainer.getName());
    beanContainers.put(beanContainer.getName(), beanContainer);
    logger.info("IOC.add(" + beanContainer.getName() + ", " + clazz.getName() + ")");
  }

  public static void set(BeanContainer beanContainer) {
    beanClasses.put(beanContainer.getClazz(), beanContainer.getName());
    beanContainers.put(beanContainer.getName(), beanContainer);
    logger.info("IOC.add(" + beanContainer.getName() + ", " + beanContainer.getClazz().getName() + ")");
  }
}
