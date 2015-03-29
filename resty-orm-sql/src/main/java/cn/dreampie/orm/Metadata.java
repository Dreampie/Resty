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

  private static String defaultDsName;

  private static Map<String, DataSourceMeta> dataSourceMetaMap = new HashMap<String, DataSourceMeta>();

  private static Map<String, TableMeta> tableMetaMap = new HashMap<String, TableMeta>();

  private static Map<Class<? extends Entity>, String> tableMetaClassMap = new HashMap<Class<? extends Entity>, String>();

  public static boolean hasDataSourceMeta(String dsName) {
    return dataSourceMetaMap.containsKey(dsName);
  }

  public static DataSourceMeta getDataSourceMeta(String dsName) {
    DataSourceMeta dsm = dataSourceMetaMap.get(dsName);
    checkNotNull(dsm, "Could not found DataSourceMetadata for this dsName:" + dsName);
    return dsm;
  }

  public static boolean hasTableMeta(String dsName, String tableName) {
    return tableMetaMap.containsKey(getMark(dsName, tableName));
  }

  public static boolean hasTableMeta(String mark) {
    return tableMetaMap.containsKey(mark);
  }

  public static TableMeta getTableMeta(String dsName, String tableName) {
    return getTableMeta(getMark(dsName, tableName));
  }

  public static TableMeta getTableMeta(Class<? extends Entity> clazz) {
    return getTableMeta(tableMetaClassMap.get(clazz));
  }

  public static TableMeta getTableMeta(String mark) {
    TableMeta mm = tableMetaMap.get(mark);
    checkNotNull(mm, "Could not found TableMetadata for this dsName" + CONNECTOR + "tableName : " + mark);
    return mm;
  }

  public static String getTableMetaMark(Class<? extends Entity> clazz) {
    return tableMetaClassMap.get(clazz);
  }

  static DataSourceMeta addDataSourceMeta(DataSourceMeta dsm) {
    String dsName = dsm.getDsName();
    checkNotNull(dsName, "DataSourceName could not be null.");
    if (dsName.contains(CONNECTOR)) {
      throw new IllegalArgumentException("DataSourceName not support '" + CONNECTOR + "' for name '" + dsName + "'.");
    }
    if (dataSourceMetaMap.size() == 0) {
      defaultDsName = dsName;
    }
    if (dataSourceMetaMap.containsKey(dsName)) {
      logger.warn("Covering multiple data sources for dsName '%s'.", dsName);
    }
    return dataSourceMetaMap.put(dsName, dsm);
  }

  static void closeDataSourceMeta() {
    for (String dsName : dataSourceMetaMap.keySet()) {
      closeDataSourceMeta(dsName);
    }
  }

  static void closeDataSourceMeta(String dsName) {
    DataSourceMeta dataSourceMeta = dataSourceMetaMap.get(dsName);
    if (dataSourceMeta != null) {
      dataSourceMeta.close();
    }
  }

  static TableMeta addTableMeta(TableMeta tableMeta) {
    return addTableMeta(tableMeta.getModelClass(), tableMeta);
  }

  static TableMeta addTableMeta(Class<? extends Entity> clazz, TableMeta tableMeta) {

    String dsName = tableMeta.getDsName();
    checkNotNull(dsName, "DataSourceName could not be null.");
    if (dsName.contains(CONNECTOR)) {
      throw new IllegalArgumentException("DataSourceName not support '" + CONNECTOR + "' for name '" + dsName + "'.");
    }
    String tableName = tableMeta.getTableName();
    checkNotNull(tableName, "TableName could not be null.");

    if (tableName.contains(CONNECTOR)) {
      throw new IllegalArgumentException("TableName not support '" + CONNECTOR + "' for name '" + tableName + "'.");
    }
    String mark = getMark(dsName, tableName);
    if (clazz != null) {
      tableMetaClassMap.put(clazz, mark);
    }
    return tableMetaMap.put(mark, tableMeta);
  }

  public static String getDefaultDsName() {
    return defaultDsName;
  }

  static String getMark(String dsName, String tableName) {
    return dsName + CONNECTOR + tableName;
  }
}
