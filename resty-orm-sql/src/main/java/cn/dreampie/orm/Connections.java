package cn.dreampie.orm;

import cn.dreampie.orm.ConnectionMetadata;

import java.util.HashMap;
import java.util.Map;

import static cn.dreampie.util.Checker.checkNotNull;

/**
 * Created by ice on 14-12-30.
 */
public class Connections {

  private static Map<String, ConnectionMetadata> connectionMetadataMap = new HashMap<String, ConnectionMetadata>();

  private static Map<Class<? extends Model>, ModelMetadata> modelMetadataMap = new HashMap<Class<? extends Model>, ModelMetadata>();

  public static ConnectionMetadata getConn(String dbName) {
    ConnectionMetadata conn = connectionMetadataMap.get(dbName);
    checkNotNull(conn, "Not found connection  for this dbName:" + dbName);
    return conn;
  }

  public static void setConnectionMetadataMap(Map<String, ConnectionMetadata> connMap) {
    connectionMetadataMap = connMap;
  }

  public static void addConnectionMetadataMap(String dbName, ConnectionMetadata conn) {
    connectionMetadataMap.put(dbName, conn);
  }

  public static void setModelMetadataMap(Map<Class<? extends Model>, ModelMetadata> modelMap) {
    modelMetadataMap = modelMap;
  }

  public static void addModelMetadataMap(Class<? extends Model> modelClass, ModelMetadata model) {
    modelMetadataMap.put(modelClass, model);
  }
}
