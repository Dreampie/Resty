package cn.dreampie.orm;

import cn.dreampie.common.entity.Entity;
import cn.dreampie.orm.annotation.Table;
import cn.dreampie.orm.dialect.Dialect;
import cn.dreampie.orm.exception.DBException;
import cn.dreampie.orm.generate.Generator;

import java.io.Serializable;
import java.util.Collections;
import java.util.SortedMap;

import static cn.dreampie.common.util.Checker.checkNotNull;


public class TableMeta implements Serializable {

  private final String generatedKey;
  private final boolean generated;
  private final Generator generator;
  private final String[] primaryKey;
  private final String tableName, dsName;
  private final Class<? extends Entity> modelClass;
  private final boolean cached;
  private final int expired;
  private SortedMap<String, ColumnMeta> columnMetadata;


  protected TableMeta(String dsName, String tableName, String generatedKey, boolean generated, Generator generator, String[] primaryKey, boolean cached, int expired) {
    this.modelClass = null;
    this.generatedKey = generatedKey;
    this.generator = generator;
    this.generated = generated;
    this.primaryKey = primaryKey;
    this.tableName = tableName;
    this.cached = cached;
    this.expired = expired;
    this.dsName = dsName;
  }

  protected TableMeta(String dsName, Class<? extends Model> modelClass) {
    Table tableAnnotation = modelClass.getAnnotation(Table.class);
    checkNotNull(tableAnnotation, "Could not found @Table Annotation.");
    this.modelClass = modelClass;
    this.generatedKey = tableAnnotation.generatedKey();
    Generator generator = null;
    try {
      generator = tableAnnotation.generator().newInstance();
    } catch (InstantiationException e) {
      throw new DBException(e.getMessage(), e);
    } catch (IllegalAccessException e) {
      throw new DBException(e.getMessage(), e);
    }
    this.generator = generator;
    this.generated = tableAnnotation.generated();
    this.primaryKey = tableAnnotation.primaryKey();
    this.tableName = tableAnnotation.name();
    this.cached = tableAnnotation.cached();
    this.expired = tableAnnotation.expired();
    this.dsName = dsName;
  }

  public String getDsName() {
    return dsName;
  }

  public boolean isCached() {
    return cached;
  }

  public boolean isGenerated() {
    return generated;
  }

  public Generator getGenerator() {
    return generator;
  }

  public Class<? extends Entity> getModelClass() {
    return modelClass;
  }

  public String getTableName() {
    return tableName;
  }

  public int getExpired() {
    return expired;
  }

  protected boolean tableExists() {
    return columnMetadata != null && columnMetadata.isEmpty();
  }

  public String getGeneratedKey() {
    return generatedKey;
  }

  public String[] getPrimaryKey() {
    return primaryKey;
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
    checkNotNull(columnMetadata, "Failed to found table: " + getTableName());
    return Collections.unmodifiableSortedMap(columnMetadata);
  }

  void setColumnMetadata(SortedMap<String, ColumnMeta> columnMetadata) {
    this.columnMetadata = columnMetadata;
  }

  public String getColumnTypeName(String columnName) {
    SortedMap<String, ColumnMeta> columnMetaSortedMap = getColumnMetadata();
    return columnMetaSortedMap.get(columnName).getTypeName();
  }

  /**
   * returns true if this attribute is present in this meta model. This method i case insensitive.
   *
   * @param column attribute name, case insensitive.
   * @return true if this attribute is present in this meta model, false of not.
   */
  public boolean hasColumn(String column) {
    return columnMetadata != null && columnMetadata.containsKey(column);
  }

  /**
   * 返回列的类型
   *
   * @param column
   * @return
   */
  public Integer getDataType(String column) {
    if (hasColumn(column)) {
      return columnMetadata.get(column).getDataType();
    }
    return null;
  }

  public String toString() {
    final StringBuilder t = new StringBuilder();
    t.append("TableMeta: ").append(tableName).append(", ").append(modelClass == null ? "Record" : modelClass).append("\n");
    if (columnMetadata != null) {
      for (String key : columnMetadata.keySet()) {
        t.append(columnMetadata.get(key)).append(", ");
      }
    }

    return t.toString();
  }
}
