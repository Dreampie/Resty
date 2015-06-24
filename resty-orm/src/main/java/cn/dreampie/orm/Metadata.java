package cn.dreampie.orm;

import cn.dreampie.log.Logger;
import cn.dreampie.orm.meta.DataSourceMeta;
import cn.dreampie.orm.meta.EntityMeta;
import cn.dreampie.orm.meta.FieldMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.dreampie.common.util.Checker.checkNotNull;

/**
 * Created by ice on 14-12-30.
 */
public class Metadata {

  private static final Logger logger = Logger.getLogger(Metadata.class);

  private static final String CONNECTOR = "@";

  private static String defaultDsName;

  private static Map<String, DataSourceMeta> dataSourceMetaMap = new HashMap<String, DataSourceMeta>();

  private static Map<String, EntityMeta> entityMetaMap = new HashMap<String, EntityMeta>();

  private static Map<Class<?>, String> entityMetaClassMap = new HashMap<Class<?>, String>();

  private static Map<Class<?>, List<FieldMeta>> fieldMap = new HashMap<Class<?>, List<FieldMeta>>();

  /**
   * 关闭所有的数据源
   */
  public static void close() {
    for (Map.Entry<String, DataSourceMeta> entry : dataSourceMetaMap.entrySet()) {
      entry.getValue().close();
    }
  }

  /**
   * 判断是不是存在数据源
   *
   * @param dsName
   * @return
   */
  public static boolean hasDataSourceMeta(String dsName) {
    return dataSourceMetaMap.containsKey(dsName);
  }

  public static DataSourceMeta getDataSourceMeta(String dsName) {
    DataSourceMeta dsm = dataSourceMetaMap.get(dsName);
    checkNotNull(dsm, "Could not found DataSourceMeta for this dsName:" + dsName);
    return dsm;
  }

  public static boolean hasEntityMeta(String dsName, String tableName) {
    return entityMetaMap.containsKey(getMark(dsName, tableName));
  }

  public static boolean hasEntityMeta(String mark) {
    return entityMetaMap.containsKey(mark);
  }

  public static EntityMeta getEntityMeta(String dsName, String tableName) {
    return getEntityMeta(getMark(dsName, tableName));
  }

  public static EntityMeta getEntityMeta(Class<?> clazz) {
    return getEntityMeta(entityMetaClassMap.get(clazz));
  }

  public static EntityMeta getEntityMeta(String mark) {
    EntityMeta mm = entityMetaMap.get(mark);
    checkNotNull(mm, "Could not found TableMeta for this dsName" + CONNECTOR + "table : " + mark);
    return mm;
  }

  public static String getEntityMetaMark(Class<?> clazz) {
    return entityMetaClassMap.get(clazz);
  }

  public static DataSourceMeta addDataSourceMeta(DataSourceMeta dsm) {
    String dsName = dsm.getDsName();
    checkNotNull(dsName, "DataSourceName could not be null.");
    if (dsName.contains(CONNECTOR)) {
      throw new IllegalArgumentException("DataSourceName not support '" + CONNECTOR + "' for table '" + dsName + "'.");
    }
    if (dataSourceMetaMap.size() == 0) {
      defaultDsName = dsName;
    }
    if (dataSourceMetaMap.containsKey(dsName)) {
      logger.error("Covering multiple dataSources for dsName '%s'.", dsName);
    }
    return dataSourceMetaMap.put(dsName, dsm);
  }

  public static EntityMeta addEntityMeta(EntityMeta entityMeta) {
    return addEntityMeta(null, entityMeta);
  }

  public static EntityMeta addEntityMeta(Class<?> clazz, EntityMeta entityMeta) {

    String dsName = entityMeta.getDsName();
    checkNotNull(dsName, "DataSourceName could not be null.");
    if (dsName.contains(CONNECTOR)) {
      throw new IllegalArgumentException("DataSourceName not support '" + CONNECTOR + "' for table '" + dsName + "'.");
    }
    String table = entityMeta.getTable();
    checkNotNull(table, "TableName could not be null.");

    if (table.contains(CONNECTOR)) {
      throw new IllegalArgumentException("TableName not support '" + CONNECTOR + "' for table '" + table + "'.");
    }
    String mark = getMark(dsName, table);
    if (clazz != null) {
      if (entityMetaClassMap.containsKey(clazz)) {
        logger.error("Covering multiple class '" + clazz + "' for table '" + table + "' in '" + dsName + "'.");
      }
      entityMetaClassMap.put(clazz, mark);
    }
    entityMetaMap.put(mark, entityMeta);
    return entityMeta;
  }

  public static void addFields(Class<?> clazz, List<FieldMeta> fieldMetas) {
    List<FieldMeta> fields = fieldMap.get(clazz);
    if (fields == null) {
      fieldMap.put(clazz, fieldMetas);
    } else {
      fields.addAll(fieldMetas);
    }
  }

  public static List<FieldMeta> getFields(Class<?> clazz) {
    return fieldMap.get(clazz);
  }

  public static String getDefaultDsName() {
    return defaultDsName;
  }

  static String getMark(String dsName, String tableName) {
    return dsName + CONNECTOR + tableName;
  }
}
