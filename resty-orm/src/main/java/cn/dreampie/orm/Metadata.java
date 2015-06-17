package cn.dreampie.orm;

import cn.dreampie.common.entity.Attrs;
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

  private static Map<Class<?>, String> tableMetaClassMap = new HashMap<Class<?>, String>();

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

  public static TableMeta getTableMeta(Class<?> clazz) {
    return getTableMeta(tableMetaClassMap.get(clazz));
  }

  public static TableMeta getTableMeta(String mark) {
    TableMeta mm = tableMetaMap.get(mark);
    checkNotNull(mm, "Could not found TableMetadata for this dsName" + CONNECTOR + "tableName : " + mark);
    return mm;
  }

  public static String getTableMetaMark(Class<?> clazz) {
    return tableMetaClassMap.get(clazz);
  }

  public static DataSourceMeta addDataSourceMeta(DataSourceMeta dsm) {
    String dsName = dsm.getDsName();
    checkNotNull(dsName, "DataSourceName could not be null.");
    if (dsName.contains(CONNECTOR)) {
      throw new IllegalArgumentException("DataSourceName not support '" + CONNECTOR + "' for tableName '" + dsName + "'.");
    }
    if (dataSourceMetaMap.size() == 0) {
      defaultDsName = dsName;
    }
    if (dataSourceMetaMap.containsKey(dsName)) {
      logger.error("Covering multiple dataSources for dsName '%s'.", dsName);
    }
    return dataSourceMetaMap.put(dsName, dsm);
  }

  static TableMeta addTableMeta(TableMeta tableMeta) {
    return addTableMeta(null, tableMeta);
  }

  static TableMeta addTableMeta(Class<?> clazz, TableMeta tableMeta) {

    String dsName = tableMeta.getDsName();
    checkNotNull(dsName, "DataSourceName could not be null.");
    if (dsName.contains(CONNECTOR)) {
      throw new IllegalArgumentException("DataSourceName not support '" + CONNECTOR + "' for tableName '" + dsName + "'.");
    }
    String tableName = tableMeta.getTableName();
    checkNotNull(tableName, "TableName could not be null.");

    if (tableName.contains(CONNECTOR)) {
      throw new IllegalArgumentException("TableName not support '" + CONNECTOR + "' for tableName '" + tableName + "'.");
    }
    String mark = getMark(dsName, tableName);
    if (clazz != null) {
      if (tableMetaClassMap.containsKey(clazz)) {
        logger.error("Covering multiple class '" + clazz + "' for tableName '" + tableName + "' in '" + dsName + "'.");
      }
      tableMetaClassMap.put(clazz, mark);
    }
    tableMetaMap.put(mark, tableMeta);
    return tableMeta;
  }

  public static String getDefaultDsName() {
    return defaultDsName;
  }

  static String getMark(String dsName, String tableName) {
    return dsName + CONNECTOR + tableName;
  }
}
