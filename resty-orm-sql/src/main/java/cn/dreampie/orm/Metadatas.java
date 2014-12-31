package cn.dreampie.orm;

import java.util.HashMap;
import java.util.Map;

import static cn.dreampie.util.Checker.checkNotNull;

/**
 * Created by ice on 14-12-30.
 */
public class Metadatas {

  private static Map<String, DataSourceMeta> dataSourceMetaMap = new HashMap<String, DataSourceMeta>();

  private static Map<Class<? extends Base>, ModelMeta> modelMetaMap = new HashMap<Class<? extends Base>, ModelMeta>();

  public static DataSourceMeta getDataSourceMeta(String dsName) {
    DataSourceMeta dsm = dataSourceMetaMap.get(dsName);
    checkNotNull(dsm, "Not found DataSourceMetadata for this dsName:" + dsName);
    return dsm;
  }

  public static ModelMeta getModelMeta(Class<? extends Base> modelClass) {
    ModelMeta mm = modelMetaMap.get(modelClass);
    checkNotNull(mm, "Not found ModelMetadata for this model:" + modelClass.getName());
    return mm;
  }

  public static void setDataSourceMetaMap(Map<String, DataSourceMeta> connMap) {
    dataSourceMetaMap = connMap;
  }

  public static void addDataSourceMeta(DataSourceMeta dsm) {
    dataSourceMetaMap.put(dsm.getDsName(), dsm);
  }

  public static void addDataSourceMeta(String dbName, DataSourceMeta conn) {
    dataSourceMetaMap.put(dbName, conn);
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
}
