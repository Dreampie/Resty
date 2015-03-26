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
  private static String defaultDsName;

  private static Map<String, DataSourceMeta> dataSourceMetaMap = new HashMap<String, DataSourceMeta>();

  private static Map<Class<? extends Entity>, TableMeta> modelTableMetaMap = new HashMap<Class<? extends Entity>, TableMeta>();

  private static Map<String, TableMeta> recordTableMetaMap = new HashMap<String, TableMeta>();

  public static boolean hasDataSourceMeta(String dsName) {
    return dataSourceMetaMap.containsKey(dsName);
  }

  public static DataSourceMeta getDataSourceMeta(String dsName) {
    DataSourceMeta dsm = dataSourceMetaMap.get(dsName);
    checkNotNull(dsm, "Could not found DataSourceMetadata for this dsName:" + dsName);
    return dsm;
  }

  public static boolean hasModelTableMeta(Class<? extends Entity> modelClass) {
    return modelTableMetaMap.containsKey(modelClass);
  }

  public static TableMeta getModelTableMeta(Class<? extends Entity> modelClass) {
    TableMeta mm = modelTableMetaMap.get(modelClass);
    checkNotNull(mm, "Could not found ModelMetadata for this model:" + modelClass.getName());
    return mm;
  }

  public static boolean hasRecordTableMeta(String dsName, String tableName) {
    return recordTableMetaMap.containsKey(getRecordMark(dsName, tableName));
  }

  public static TableMeta getRecordTableMeta(String dsName, String tableName) {
    TableMeta mm = recordTableMetaMap.get(getRecordMark(dsName, tableName));
    checkNotNull(mm, "Could not found ModelMetadata for this dsName : " + dsName + ", tableName : " + tableName);
    return mm;
  }

  public static void setDataSourceMetaMap(Map<String, DataSourceMeta> connMap) {
    dataSourceMetaMap = connMap;
  }

  public static void addDataSourceMeta(DataSourceMeta dsm) {
    dataSourceMetaMap.put(dsm.getDsName(), dsm);
  }

  public static void addDataSourceMeta(String dsName, DataSourceMeta conn) {
    if (dataSourceMetaMap.size() == 0) {
      defaultDsName = dsName;
    }
    if (dataSourceMetaMap.containsKey(dsName))
      logger.warn("Covering multiple data sources for dsName '%s'.", dsName);
    dataSourceMetaMap.put(dsName, conn);
  }

  public static void closeDataSourceMeta() {
    for (String dsName : dataSourceMetaMap.keySet()) {
      closeDataSourceMeta(dsName);
    }
  }

  public static void closeDataSourceMeta(String dsName) {
    DataSourceMeta dataSourceMeta = dataSourceMetaMap.get(dsName);
    if (dataSourceMeta != null) {
      dataSourceMeta.close();
    }
  }

  public static void setModelTableMetaMap(Map<Class<? extends Entity>, TableMeta> tableMetaMap) {
    modelTableMetaMap = tableMetaMap;
  }


  public static void addModelTableMeta(Class<? extends Entity> modelClass, TableMeta tableMeta) {
    modelTableMetaMap.put(modelClass, tableMeta);
  }

  public static void setRecordTableMetaMap(Map<String, TableMeta> tableMetaMap) {
    recordTableMetaMap = tableMetaMap;
  }

  public static void addRecordTableMeta(String dsName, String tableName, TableMeta tableMeta) {
    recordTableMetaMap.put(getRecordMark(dsName, tableName), tableMeta);
  }

  public static String getDefaultDsName() {
    return defaultDsName;
  }

  public static String getRecordMark(String dsName, String tableName) {
    return dsName + "@" + tableName;
  }
}
