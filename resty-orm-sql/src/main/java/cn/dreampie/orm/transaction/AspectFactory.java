package cn.dreampie.orm.transaction;


import cn.dreampie.log.Logger;
import cn.dreampie.log.LoggerFactory;
import cn.dreampie.util.Joiner;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangrenhui on 15/1/2.
 * 代理工厂类
 */
public class AspectFactory {

  private static final Logger logger = LoggerFactory.getLogger(AspectFactory.class);

  /**
   * 私有构造方法
   */
  private AspectFactory() {
  }

  /**
   * 工厂方法
   *
   * @param instance 代理目标类实例对象
   * @param aspects  切面集合
   */
  public static <T> T newInstance(T instance, Aspect... aspects) {
    AspectHandler hander = new AspectHandler(instance, aspects);
    Class<?> clazz = instance.getClass();
    if (logger.isDebugEnabled()) {
      logger.debug("Instance of " + clazz + ",interfaces:" + Joiner.on(",").join(clazz.getInterfaces()));
    }
    return (T) Proxy.newProxyInstance(clazz.getClassLoader(),
        clazz.getInterfaces(),
        hander);
  }
}