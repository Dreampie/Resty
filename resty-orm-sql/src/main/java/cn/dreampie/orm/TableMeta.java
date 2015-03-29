package cn.dreampie.orm;

import cn.dreampie.common.entity.Entity;
import cn.dreampie.log.Logger;
import cn.dreampie.orm.annotation.Table;
import cn.dreampie.orm.dialect.Dialect;

import java.io.Serializable;
import java.util.Collections;
import java.util.SortedMap;

import static cn.dreampie.common.util.Checker.checkNotNull;


public class TableMeta implements Serializable {
  private final static Logger logger = Logger.getLogger(TableMeta.class);

  private SortedMap<String, ColumnMeta> columnMetadata;
  private final String pKeys;
  private final String primaryKey;
  private final String[] primaryKeys;
  private final boolean lockKey;
  private final String tableName, dsName;
  private final Class<? extends Entity> modelClass;
  private final boolean cached;

  protected TableMeta(String dsName, String tableName, String pKeys, boolean lKey, boolean cached) {
    this.modelClass = null;
    this.pKeys = pKeys;
    if (pKeys.contains(",")) {
      this.lockKey = lKey;
      this.primaryKeys = pKeys.split(",");
      this.primaryKey = primaryKeys[0];
    } else {
      this.primaryKeys = null;
      this.lockKey = false;
      this.primaryKey = pKeys;
    }
    this.tableName = tableName;
    this.cached = cached;
    this.dsName = dsName;
  }

  protected TableMeta(String dsName, Class<? extends Model> modelClass) {
    Table tableAnnotation = modelClass.getAnnotation(Table.class);
    checkNotNull(tableAnnotation, "Could not found @Table Annotation.");
    this.modelClass = modelClass;
    this.pKeys = tableAnnotation.primaryKey();
    if (this.pKeys.contains(",")) {
      this.lockKey = tableAnnotation.lockKey();
      this.primaryKeys = this.pKeys.split(",");
      this.primaryKey = this.primaryKeys[0];
    } else {
      this.primaryKeys = null;
      this.lockKey = false;
      this.primaryKey = this.pKeys;
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

  public Class<? extends Entity> getModelClass() {
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

  public String getpKeys() {
    return pKeys;
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
    return Metadata.getDataSourceMeta(dsName).getDialect().getDbType();
  }

  public Dialect getDialect() {
    return Metadata.getDataSourceMeta(dsName).getDialect();
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
