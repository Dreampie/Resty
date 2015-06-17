package cn.dreampie.common.util;

import cn.dreampie.common.entity.exception.EntityException;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dreampie
 * @date 2015-06-12
 * @what
 */
public class Attributor {

  /**
   * 通过反射 设置属性
   *
   * @param obj
   * @param attrs
   */
  public static void setAttrs(Object obj, Map<String, Object> attrs) {
    Class clazz = obj.getClass();
    PropertyDescriptor propertyDescriptor;
    Method method;
    try {
      for (Field field : clazz.getDeclaredFields()) {
        propertyDescriptor = new PropertyDescriptor(field.getName(), clazz);
        //获得get方法
        method = propertyDescriptor.getWriteMethod();
        method.invoke(obj, attrs.get(field.getName()));
      }
    } catch (IntrospectionException e) {
      throw new EntityException(e.getMessage(), e);
    } catch (InvocationTargetException e) {
      throw new EntityException(e.getMessage(), e);
    } catch (IllegalAccessException e) {
      throw new EntityException(e.getMessage(), e);
    }
  }

  /**
   * 通过反射获取属性
   *
   * @param obj
   * @return
   */
  public static Map<String, Object> getAttrs(Object obj) {
    Class clazz = obj.getClass();
    Map<String, Object> attrs = new HashMap<String, Object>();
    PropertyDescriptor propertyDescriptor;
    Method method;

    try {
      for (Field field : clazz.getDeclaredFields()) {
        propertyDescriptor = new PropertyDescriptor(field.getName(), clazz);
        //获得get方法
        method = propertyDescriptor.getReadMethod();
        attrs.put(field.getName(), method.invoke(obj));
      }
    } catch (IntrospectionException e) {
      throw new EntityException(e.getMessage(), e);
    } catch (InvocationTargetException e) {
      throw new EntityException(e.getMessage(), e);
    } catch (IllegalAccessException e) {
      throw new EntityException(e.getMessage(), e);
    }
    return attrs;
  }

  /**
   * 获取所有的属性名字
   *
   * @param obj
   * @return
   */
  public static String[] getAttrNames(Object obj) {
    Class clazz = obj.getClass();
    String[] attrNames = new String[clazz.getDeclaredFields().length];

    int i = 0;
    for (Field field : clazz.getDeclaredFields()) {
      //获得get方法
      attrNames[i++] = field.getName();
    }
    return attrNames;
  }

  /**
   * 获取所有的属性值
   *
   * @param obj
   * @return
   */
  public static Object[] getAttrValues(Object obj) {
    Class clazz = obj.getClass();
    Object[] attrValues = new Object[clazz.getDeclaredFields().length];
    PropertyDescriptor propertyDescriptor;
    Method method;

    try {
      int i = 0;
      for (Field field : clazz.getDeclaredFields()) {
        propertyDescriptor = new PropertyDescriptor(field.getName(), clazz);
        //获得get方法
        method = propertyDescriptor.getReadMethod();
        attrValues[i++] = method.invoke(obj);
      }
    } catch (IntrospectionException e) {
      throw new EntityException(e.getMessage(), e);
    } catch (InvocationTargetException e) {
      throw new EntityException(e.getMessage(), e);
    } catch (IllegalAccessException e) {
      throw new EntityException(e.getMessage(), e);
    }
    return attrValues;
  }

  /**
   * 设置属性
   *
   * @param obj
   * @param field
   * @param val
   */
  public static void setAttr(Object obj, String field, Object val) {
    Class clazz = obj.getClass();
    PropertyDescriptor propertyDescriptor;
    Method method;
    try {
      propertyDescriptor = new PropertyDescriptor(field, clazz);
      //获得get方法
      method = propertyDescriptor.getWriteMethod();
      method.invoke(obj, val);
    } catch (IntrospectionException e) {
      throw new EntityException(e.getMessage(), e);
    } catch (InvocationTargetException e) {
      throw new EntityException(e.getMessage(), e);
    } catch (IllegalAccessException e) {
      throw new EntityException(e.getMessage(), e);
    }
  }

  /**
   * 获取属性值
   *
   * @param obj
   * @param field
   * @return
   */
  public static Object getAttr(Object obj, String field) {
    Class clazz = obj.getClass();
    Object result = null;
    try {
      PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field, clazz);
      result = propertyDescriptor.getReadMethod().invoke(obj);
    } catch (IntrospectionException e) {
      throw new EntityException(e.getMessage(), e);
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    return result;
  }
}
