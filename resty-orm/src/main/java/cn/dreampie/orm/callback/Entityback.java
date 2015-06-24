package cn.dreampie.orm.callback;

import cn.dreampie.common.entity.exception.EntityException;
import cn.dreampie.orm.Metadata;
import cn.dreampie.orm.meta.EntityMeta;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by ice on 14-12-30.
 */
public class Entityback implements Resultback {

  private Class<?> entityClass;

  public Entityback(Class<?> entityClass) {
    this.entityClass = entityClass;
  }

  public <T> List<T> call(ResultSet rs) throws SQLException {
    List<T> result = new ArrayList<T>();
    ResultSetMetaData rsmd = rs.getMetaData();
    int columnCount = rsmd.getColumnCount();
    String[] labelNames = new String[columnCount + 1];
    int[] types = new int[columnCount + 1];
    buildLabelNamesAndTypes(rsmd, labelNames, types);

    T entity;
    EntityMeta entityMeta = Metadata.getEntityMeta(entityClass);
    Map<String, Field> fields = entityMeta.getFields();

    try {
      while (rs.next()) {
        entity = (T) entityClass.newInstance();
        for (int i = 1; i <= columnCount; i++) {
          fields.get(labelNames[i]).set(entity, rs.getObject(i));
        }
        result.add(entity);
      }
    } catch (InstantiationException e) {
      throw new EntityException(e.getMessage(), e);
    } catch (IllegalAccessException e) {
      throw new EntityException(e.getMessage(), e);
    }
    return result;
  }

  private void buildLabelNamesAndTypes(ResultSetMetaData rsmd, String[] labelNames, int[] types) throws SQLException {
    for (int i = 1; i < labelNames.length; i++) {
      labelNames[i] = rsmd.getColumnLabel(i);
      types[i] = rsmd.getColumnType(i);
    }
  }

}
