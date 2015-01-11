package cn.dreampie.orm;

import cn.dreampie.log.Logger;
import cn.dreampie.orm.annotation.Table;
import cn.dreampie.orm.dialect.Dialect;

import java.io.Serializable;
import java.util.Collections;
import java.util.SortedMap;

import static cn.dreampie.common.util.Checker.checkNotNull;


public class ModelMeta implements Serializable {
  private final static Logger logger = Logger.getLogger(ModelMeta.class);

  private SortedMap<String, ColumnMeta> columnMetadata;
  private final String primaryKey;
  private final String[] primaryKeys;
  private final boolean lockKey;
  private final String tableName, dsName;
  private final Class<? extends Base> modelClass;
  private final boolean cached;

  protected ModelMeta(Class<? extends Base> modelClass, String dsName) {
    Table tableAnnotation = modelClass.getAnnotation(Table.class);
    checkNotNull(tableAnnotation, "Could not found @Table Annotation.");
    this.modelClass = modelClass;
    String key = tableAnnotation.primaryKey();

    if (key.contains(",")) {
      primaryKeys = key.split(",");
      lockKey = tableAnnotation.lockKey();
      this.primaryKey = primaryKeys[0];
    } else {
      primaryKeys = null;
      lockKey = false;
      this.primaryKey = key;
    }
    this.tableName = tableAnnotation.name();
    this.cached = tableAnnotation.cached();
    this.dsName = dsName;
  }

  public String getDsName() {
    return dsName;
  }

  public boolean isCached() {
    return cached;
  }

  public Class<? extends Base> getModelClass() {
    return modelClass;
  }

  public String getTableName() {
    return tableName;
  }

  void setColumnMetadata(SortedMap<String, ColumnMeta> columnMetadata) {
    this.columnMetadata = columnMetadata;
  }

  protected boolean tableExists() {
    return columnMetadata != null && columnMetadata.isEmpty();
  }


  public String getPrimaryKey() {
    return primaryKey;
  }

  public String[] getPrimaryKeys() {
    return primaryKeys;
  }

  public boolean isLockKey() {
    return lockKey;
  }

  public String getDbType() {
    return Metadatas.getDataSourceMeta(dsName).getDialect().getDbType();
  }

  public Dialect getDialect() {
    return Metadatas.getDataSourceMeta(dsName).getDialect();
  }

  /**
   * Provides column metadata map, keyed by attribute name.
   * Table columns correspond to ActiveJDBC model attributes.
   *
   * @return Provides column metadata map, keyed by attribute name.
   */
  public SortedMap<String, ColumnMeta> getColumnMetadata() {
    checkNotNull(columnMetadata, "Failed to find table: " + getTableName());
    return Collections.unmodifiableSortedMap(columnMetadata);
  }

  public String getColumnTypeName(String columnName) {
    SortedMap<String, ColumnMeta> columnMetaSortedMap = getColumnMetadata();
    return columnMetaSortedMap.get(columnName).getTypeName();
  }

  /**
   * returns true if this attribute is present in this meta model. This method i case insensitive.
   *
   * @param attribute attribute name, case insensitive.
   * @return true if this attribute is present in this meta model, false of not.
   */
  boolean hasAttribute(String attribute) {
    return columnMetadata != null && columnMetadata.containsKey(attribute);
  }


  public String toString() {
    final StringBuilder t = new StringBuilder();
    t.append("MetaModel: ").append(tableName).append(", ").append(modelClass).append("\n");
    if (columnMetadata != null) {
      for (String key : columnMetadata.keySet()) {
        t.append(columnMetadata.get(key)).append(", ");
      }
    }

    return t.toString();
  }
}
