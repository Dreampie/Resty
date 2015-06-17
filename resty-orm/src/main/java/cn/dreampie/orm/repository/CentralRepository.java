package cn.dreampie.orm.repository;

import cn.dreampie.orm.exception.DBException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author Dreampie
 * @date 2015-06-14
 * @what
 */
public class CentralRepository<E> {

  public Class<E> getEntityClass() {
    Class clazz = getClass();
    Type[] actualTypeArguments = ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments();
    if (actualTypeArguments.length > 0) {
      return (Class<E>) actualTypeArguments[0];
    } else {
      throw new DBException("Could not found entity.");
    }
  }


}
