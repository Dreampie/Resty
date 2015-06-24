package cn.dreampie.orm.repository;

import cn.dreampie.common.entity.exception.EntityException;
import cn.dreampie.orm.Metadata;
import cn.dreampie.orm.callback.Entityback;
import cn.dreampie.orm.meta.FieldMeta;
import cn.dreampie.orm.meta.IdMeta;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Dreampie
 * @date 2015-06-17
 * @what
 */
public class EntitySqlBuilder extends SqlBuilder {

  private Class<?> entityClass;

  public EntitySqlBuilder(Class<?> entityClass) {
    super(Metadata.getEntityMeta(entityClass));
    this.entityClass = entityClass;
  }

  public <T> List<T> list() {
    return select("*").from(entityMeta.getTable()).query(new Entityback(entityClass));
  }

  public int save(Object entity) {
    int result = -1;
    if (entityClass == entity.getClass()) {
      Map<String, Field> fields = entityMeta.getFields();
      List<String> columns = new ArrayList<String>();
      Object value;
      try {
        for (Map.Entry<String, Field> fieldsEntry : fields.entrySet()) {

          value = fieldsEntry.getValue().get(entity);
          if (value != null) {
            params.add(value);
            columns.add(fieldsEntry.getKey());
          }
        }
      } catch (IllegalAccessException e) {
        throw new EntityException(e.getMessage(), e);
      }

      int len = columns.size();
      result = insert(entityMeta.getTable()).columns(columns).values(len).update();
    }
    return result;
  }

  public int update(Object entity) {
    int result = -1;
    if (entityClass == entity.getClass()) {
      IdMeta idMeta = entityMeta.getIdMeta();
      Map<String, Field> fields = entityMeta.getFields();
      List<String> columns = new ArrayList<String>();
      Object value;
      Field field;
      FieldMeta fieldMeta;
      try {
        for (Map.Entry<String, Field> fieldsEntry : fields.entrySet()) {
          field = fieldsEntry.getValue();
          fieldMeta = entityMeta.getFieldMetas().get(field);
          if (!(fieldMeta instanceof IdMeta)) {
            value = field.get(entity);
            if (value != null) {
              params.add(value);
              columns.add(fieldsEntry.getKey() + " = ?");
            }
          }
        }

        if (idMeta != null) {
          params.add(idMeta.getField().get(entity));
          result = update(entityMeta.getTable()).set(columns).where(idMeta.getColumn() + " = ?").update();
        }
      } catch (IllegalAccessException e) {
        throw new EntityException(e.getMessage(), e);
      }
    }
    return result;
  }

  public int delete(Object entity) {
    int result = -1;
    if (entityClass == entity.getClass()) {
      IdMeta idMeta = entityMeta.getIdMeta();
      try {
        params.add(idMeta.getField().get(entity));
        result = delete(entityMeta.getTable()).where(idMeta.getColumn() + " = ?").update();
      } catch (IllegalAccessException e) {
        throw new EntityException(e.getMessage(), e);
      }
    }
    return result;
  }


}
