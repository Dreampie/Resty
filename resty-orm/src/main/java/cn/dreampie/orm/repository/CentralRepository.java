package cn.dreampie.orm.repository;

import cn.dreampie.orm.Metadata;
import cn.dreampie.orm.exception.DBException;
import cn.dreampie.orm.meta.EntityMeta;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author Dreampie
 * @date 2015-06-14
 * @what
 */
public class CentralRepository<E> {

  /**
   * 获取实体Class
   *
   * @return
   */
  public Class<E> getEntityClass() {
    Class clazz = getClass();
    Type[] actualTypeArguments = ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments();
    if (actualTypeArguments.length > 0) {
      return (Class<E>) actualTypeArguments[0];
    } else {
      throw new DBException("Could not found entity.");
    }
  }

  /**
   * 获取table元数据
   *
   * @return
   */
  public EntityMeta getEntityMeta() {
    Class<E> entityClass = getEntityClass();
    return Metadata.getEntityMeta(entityClass);
  }

  public SqlBuilder build(String sql) {
    return new EntitySqlBuilder(getEntityClass()).build(sql);
  }

  public SqlBuilder insert(String table) {
    return new EntitySqlBuilder(getEntityClass()).insert(table);
  }

  public SqlBuilder delete(String table) {
    return new EntitySqlBuilder(getEntityClass()).delete(table);
  }

  public SqlBuilder update(String table) {
    return new EntitySqlBuilder(getEntityClass()).update(table);
  }

  public SqlBuilder select(String... columns) {
    return new EntitySqlBuilder(getEntityClass()).select(columns);
  }

  public int save(E entity) {
    return new EntitySqlBuilder(getEntityClass()).save(entity);
  }

  public int delete(E entity) {
    return new EntitySqlBuilder(getEntityClass()).delete(entity);
  }

  public int update(E entity) {
    return new EntitySqlBuilder(getEntityClass()).update(entity);
  }

  public List<E> list(E entity) {
    return new EntitySqlBuilder(getEntityClass()).list();
  }
}
