package cn.dreampie.orm;

import java.util.HashMap;
import java.util.Map;

import static cn.dreampie.util.Checker.checkNotNull;

/**
 * Created by ice on 14-12-30.
 */
public class Metadatas {

  private static Map<String, DataSourceMeta> dataSourceMetadataMap = new HashMap<String, DataSourceMeta>();

  private static Map<Class<? extends Model>, ModelMeta> modelMetadataMap = new HashMap<Class<? extends Model>, ModelMeta>();

  public static DataSourceMeta getDataSourceMetadata(String dsName) {
    DataSourceMeta dsm = dataSourceMetadataMap.get(dsName);
    checkNotNull(dsm, "Not found DataSourceMetadata for this dsName:" + dsName);
    return dsm;
  }

  public static ModelMeta getModelMetadata(Class<? extends Model> modelClass) {
    ModelMeta mm = modelMetadataMap.get(modelClass);
    checkNotNull(mm, "Not found ModelMetadata for this model:" + modelClass.getName());
    return mm;
  }

  public static void setDataSourceMetadataMap(Map<String, DataSourceMeta> connMap) {
    dataSourceMetadataMap = connMap;
  }

  public static void addDataSourceMetadata(DataSourceMeta dsm) {
    dataSourceMetadataMap.put(dsm.getDsName(), dsm);
  }

  public static void addDataSourceMetadata(String dbName, DataSourceMeta conn) {
    dataSourceMetadataMap.put(dbName, conn);
  }

  public static void setModelMetadataMap(Map<Class<? extends Model>, ModelMeta> modelMap) {
    modelMetadataMap = modelMap;
  }

  public static void addModelMetadata(ModelMeta model) {
    modelMetadataMap.put(model.getModelClass(), model);
  }

  public static void addModelMetadata(Class<? extends Model> modelClass, ModelMeta model) {
    modelMetadataMap.put(modelClass, model);
  }
}
