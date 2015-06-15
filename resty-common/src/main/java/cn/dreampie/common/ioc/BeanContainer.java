package cn.dreampie.common.ioc;

import cn.dreampie.common.annotation.Autowired;
import cn.dreampie.common.annotation.Repository;
import cn.dreampie.common.annotation.Resource;
import cn.dreampie.common.annotation.Service;
import cn.dreampie.common.ioc.exception.IOCException;
import cn.dreampie.common.util.Checker;
import cn.dreampie.common.util.Stringer;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * @author Dreampie
 * @date 2015-06-12
 * @what
 */
public class BeanContainer implements Serializable {

  private String name;
  private Class<?> clazz;
  private boolean singleton;
  private Object bean;

  public BeanContainer(Class<?> clazz) {
    this.clazz = clazz;

    Resource resourceAnn = clazz.getAnnotation(Resource.class);
    Service serviceAnn = clazz.getAnnotation(Service.class);
    Repository repositoryAnn = clazz.getAnnotation(Repository.class);
    if (resourceAnn != null) {
      this.name = resourceAnn.name();
      if (this.name.isEmpty()) {
        this.name = Stringer.firstLowerCase(clazz.getSimpleName());
      }
      this.singleton = resourceAnn.singleton();

    } else if (serviceAnn != null) {
      this.name = serviceAnn.name();
      if (this.name.isEmpty()) {
        this.name = Stringer.firstLowerCase(clazz.getSimpleName());
      }
      this.singleton = serviceAnn.singleton();
    } else if (repositoryAnn != null) {
      this.name = repositoryAnn.name();
      if (this.name.isEmpty()) {
        this.name = Stringer.firstLowerCase(clazz.getSimpleName());
      }
      this.singleton = repositoryAnn.singleton();
    }
    Checker.checkNotNull(name, "Could not found any ioc annotation.");

    if (singleton) {
      bean = instance(clazz);
    }
  }

  /**
   * 注入bean
   *
   * @param clazz
   */
  private void autowired(Object bean, Class<?> clazz) {
    Object obj;
    String name = null;
    boolean required = true;
    Autowired autowired;
    Class<?> autowiredClass;
    for (Field field : clazz.getDeclaredFields()) {
      autowired = field.getAnnotation(Autowired.class);
      if (autowired != null) {
        name = autowired.name();
        required = autowired.required();
        autowiredClass=field.getDeclaringClass();
        // 不用根据set和get共有方法进行private属性设置，因为用户写起来太麻烦，直接暂时暴力破坏封装，通过提供
        // “可访问权限”来设置该属性
        field.setAccessible(true);
        obj = ApplicationContainer.get(name.isEmpty() ? field.getName() : name);

        if (obj == null) {
          obj = ApplicationContainer.get(autowiredClass);
        }

        if (obj == null && required) {
          throw new IOCException("Could not autowired class " + clazz + ": " + field.getName() + ".");
        }
        try {
          field.set(bean, obj);
        } catch (IllegalAccessException e) {
          throw new IOCException(e.getMessage(), e);
        }
      }
    }
  }

  /**
   * 初始化对象
   *
   * @param clazz
   * @return
   */
  private Object instance(Class<?> clazz) {
    try {
      return clazz.newInstance();
    } catch (InstantiationException e) {
      throw new IOCException(e.getMessage(), e);
    } catch (IllegalAccessException e) {
      throw new IOCException(e.getMessage(), e);
    }
  }

  public String getName() {
    return name;
  }

  public Class<?> getClazz() {
    return clazz;
  }

  public boolean isSingleton() {
    return singleton;
  }

  public <T> T get() {
    T result = null;
    if (singleton) {
      result = (T) bean;
    } else {
      result = (T) instance(clazz);
    }
    //注入对象
    autowired(bean, clazz);
    return result;
  }

}
