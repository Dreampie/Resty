package cn.dreampie.orm;

import cn.dreampie.common.entity.Entity;
import cn.dreampie.orm.annotation.Table;
import cn.dreampie.orm.dialect.Dialect;
import cn.dreampie.orm.generate.Generator;
import cn.dreampie.orm.generate.GeneratorFactory;

import java.io.Serializable;
import java.util.Collections;
import java.util.SortedMap;

import static cn.dreampie.common.util.Checker.checkNotNull;


public class TableMeta implements Serializable {

  private final String dsmName, tableName;
  private final String generatedKey;
  private final String[] primaryKey;
  private final Generator generator;
  private final boolean cached;
  private final int expired;
  private final String sequence;
  private final Class<? extends Entity> modelClass;
  private SortedMap<String, ColumnMeta> columnMetadata;
  private TableSetting tableSetting;

  protected TableMeta(String dsmName) {
    this.dsmName = dsmName;
    this.tableName = null;
    this.generatedKey = "";
    this.primaryKey = new String[]{};
    this.generator = null;
    this.cached = false;
    this.expired = -1;
    this.sequence = null;
    this.modelClass = null;
    this.tableSetting = null;
  }

  protected TableMeta(TableSetting tableSetting) {
    this(Metadata.getDefaultDsmName(), tableSetting);
  }

  protected TableMeta(String dsmName, TableSetting tableSetting) {
    this.dsmName = dsmName;
    this.tableName = tableSetting.getTableName();
    this.generatedKey = tableSetting.getGeneratedKey();
    this.primaryKey = tableSetting.getPrimaryKey();
    this.generator = tableSetting.getGenerator();
    this.cached = tableSetting.isCached();
    this.expired = tableSetting.getExpired();
    this.sequence = tableSetting.getSequence();
    this.modelClass = null;
    this.tableSetting = tableSetting;
  }

  protected TableMeta(String dsmName, Class<? extends Model> modelClass) {
    Table tableAnnotation = modelClass.getAnnotation(Table.class);
    checkNotNull(tableAnnotation, "Could not found @Table Annotation.");
    this.dsmName = dsmName;
    this.tableName = tableAnnotation.name();
    this.generatedKey = tableAnnotation.generatedKey();
    this.primaryKey = tableAnnotation.primaryKey();
    this.generator = GeneratorFactory.get(tableAnnotation.generatedType());
    this.cached = tableAnnotation.cached();
    this.expired = tableAnnotation.expired();
    this.sequence = tableAnnotation.sequence();
    this.modelClass = modelClass;
    this.tableSetting = new TableSetting(tableName, generatedKey, primaryKey, generator, cached, expired, sequence);
  }

  public String getDsmName() {
    return dsmName;
  }

  public boolean isCached() {
    return cached;
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
    return Metadata.getDataSourceMeta(dsmName).getDialect().getDbType();
  }

  public Dialect getDialect() {
    return Metadata.getDataSourceMeta(dsmName).getDialect();
  }

  public String getSequence() {
    return sequence;
  }

  public TableSetting getTableSetting() {
    return tableSetting;
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
