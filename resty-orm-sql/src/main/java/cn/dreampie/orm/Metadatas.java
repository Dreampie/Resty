package cn.dreampie.orm;

import cn.dreampie.log.Logger;
import cn.dreampie.orm.exception.DBException;

import java.util.HashMap;
import java.util.Map;

import static cn.dreampie.common.util.Checker.checkNotNull;

/**
 * Created by ice on 14-12-30.
 */
public class Metadatas {

  private static final Logger logger = Logger.getLogger(Metadatas.class);
  private static String defaultDsName;

  private static Map<String, DataSourceMeta> dataSourceMetaMap = new HashMap<String, DataSourceMeta>();

  private static Map<Class<? extends Base>, ModelMeta> modelMetaMap = new HashMap<Class<? extends Base>, ModelMeta>();

  public static DataSourceMeta getDataSourceMeta(String dsName) {
    DataSourceMeta dsm = dataSourceMetaMap.get(dsName);
    checkNotNull(dsm, "Could not found DataSourceMetadata for this dsName:" + dsName);
    return dsm;
  }

  public static ModelMeta getModelMeta(Class<? extends Base> modelClass) {
    ModelMeta mm = modelMetaMap.get(modelClass);
    checkNotNull(mm, "Could not found ModelMetadata for this model:" + modelClass.getName());
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

  public static void setModelMetaMap(Map<Class<? extends Base>, ModelMeta> modelMap) {
    modelMetaMap = modelMap;
  }

  public static void addModelMeta(ModelMeta model) {
    modelMetaMap.put(model.getModelClass(), model);
  }

  public static void addModelMeta(Class<? extends Base> modelClass, ModelMeta model) {
    modelMetaMap.put(modelClass, model);
  }

  public static String getDefaultDsName() {
    return defaultDsName;
  }
}
