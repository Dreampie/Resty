package cn.dreampie.orm;

import cn.dreampie.common.entity.Entity;
import cn.dreampie.log.Logger;

import java.util.HashMap;
import java.util.Map;

import static cn.dreampie.common.util.Checker.checkNotNull;

/**
 * Created by ice on 14-12-30.
 */
public class Metadata {

  private static final Logger logger = Logger.getLogger(Metadata.class);

  private static final String CONNECTOR = "@";

  private static String defaultDsmName;

  private static Map<String, DataSourceMeta> dataSourceMetaMap = new HashMap<String, DataSourceMeta>();

  private static Map<String, TableMeta> tableMetaMap = new HashMap<String, TableMeta>();

  private static Map<Class<? extends Entity>, String> tableMetaClassMap = new HashMap<Class<? extends Entity>, String>();

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
   * @param dsmName
   * @return
   */
  public static boolean hasDataSourceMeta(String dsmName) {
    return dataSourceMetaMap.containsKey(dsmName);
  }

  public static DataSourceMeta getDataSourceMeta(String dsmName) {
    DataSourceMeta dsm = dataSourceMetaMap.get(dsmName);
    checkNotNull(dsm, "Could not found DataSourceMetadata for this dsmName:" + dsmName);
    return dsm;
  }

  public static boolean hasTableMeta(String dsmName, String tableName) {
    return tableMetaMap.containsKey(getMark(dsmName, tableName));
  }

  public static boolean hasTableMeta(String mark) {
    return tableMetaMap.containsKey(mark);
  }

  public static TableMeta getTableMeta(String dsmName, String tableName) {
    return getTableMeta(getMark(dsmName, tableName));
  }

  public static TableMeta getTableMeta(Class<? extends Entity> clazz) {
    return getTableMeta(tableMetaClassMap.get(clazz));
  }

  public static TableMeta getTableMeta(String mark) {
    TableMeta mm = tableMetaMap.get(mark);
    checkNotNull(mm, "Could not found TableMetadata for this dsmName" + CONNECTOR + "tableName : " + mark);
    return mm;
  }

  public static String getTableMetaMark(Class<? extends Entity> clazz) {
    return tableMetaClassMap.get(clazz);
  }

  static DataSourceMeta addDataSourceMeta(DataSourceMeta dsm) {
    String dsmName = dsm.getDsmName();
    checkNotNull(dsmName, "DataSourceMetaName could not be null.");
    if (dsmName.contains(CONNECTOR)) {
      throw new IllegalArgumentException("DataSourceMetaName not support '" + CONNECTOR + "' for name '" + dsmName + "'.");
    }
    if (dataSourceMetaMap.size() == 0) {
      defaultDsmName = dsmName;
    }
    if (dataSourceMetaMap.containsKey(dsmName)) {
      logger.error("Covering multiple dataSources for dsmName '%s'.", dsmName);
    }
    return dataSourceMetaMap.put(dsmName, dsm);
  }

  static TableMeta addTableMeta(TableMeta tableMeta) {
    return addTableMeta(tableMeta.getModelClass(), tableMeta);
  }

  static TableMeta addTableMeta(Class<? extends Entity> clazz, TableMeta tableMeta) {

    String dsmName = tableMeta.getDsmName();
    checkNotNull(dsmName, "DataSourceMetaName could not be null.");
    if (dsmName.contains(CONNECTOR)) {
      throw new IllegalArgumentException("DataSourceMetaName not support '" + CONNECTOR + "' for name '" + dsmName + "'.");
    }
    String tableName = tableMeta.getTableName();
    checkNotNull(tableName, "TableName could not be null.");

    if (tableName.contains(CONNECTOR)) {
      throw new IllegalArgumentException("TableName not support '" + CONNECTOR + "' for name '" + tableName + "'.");
    }
    String mark = getMark(dsmName, tableName);
    if (clazz != null) {
      if (tableMetaClassMap.containsKey(clazz)) {
        logger.error("Covering multiple class '" + clazz + "' for table '" + tableName + "' in '" + dsmName + "'.");
      }
      tableMetaClassMap.put(clazz, mark);
    }
    tableMetaMap.put(mark, tableMeta);
    return tableMeta;
  }

  public static String getDefaultDsmName() {
    return defaultDsmName;
  }

  static String getMark(String dsmName, String tableName) {
    return dsmName + CONNECTOR + tableName;
  }
}
