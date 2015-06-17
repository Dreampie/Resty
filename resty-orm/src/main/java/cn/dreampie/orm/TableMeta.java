package cn.dreampie.orm;

import cn.dreampie.common.generate.Generator;
import cn.dreampie.orm.dialect.Dialect;
import cn.dreampie.orm.repository.GenerateType;

import java.io.Serializable;
import java.util.Collections;
import java.util.SortedMap;

import static cn.dreampie.common.util.Checker.checkNotNull;


public class TableMeta implements Serializable {

  private final String id;
  private final GenerateType generateType;
  private final Generator generator;
  private final String tableName, dsName;
  private final boolean cached;
  private SortedMap<String, ColumnMeta> columnMetadata;


  public TableMeta(String dsName, String tableName, String id, GenerateType generateType, Generator generator, boolean cached) {
    this.id = id;
    this.generateType = generateType;
    this.generator = generator;
    this.tableName = tableName;
    this.cached = cached;
    this.dsName = dsName;
  }

  public String getDsName() {
    return dsName;
  }

  public boolean isCached() {
    return cached;
  }

  public GenerateType getGenerateType() {
    return generateType;
  }

  public Generator getGenerator() {
    return generator;
  }

  public String getTableName() {
    return tableName;
  }

  protected boolean tableExists() {
    return columnMetadata != null && columnMetadata.isEmpty();
  }

  public String getId() {
    return id;
  }

  public String getDbType() {
    return Metadata.getDataSourceMeta(dsName).getDialect().getDbType();
  }

  public Dialect getDialect() {
    return Metadata.getDataSourceMeta(dsName).getDialect();
  }

  /**
   * Provides column metadata map, keyed by attribute tableName.
   * Entity columns correspond to ActiveJDBC model attributes.
   *
   * @return Provides column metadata map, keyed by attribute tableName.
   */
  public SortedMap<String, ColumnMeta> getColumnMetadata() {
    checkNotNull(columnMetadata, "Failed to found tableName: " + getTableName());
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
   * @param column attribute tableName, case insensitive.
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
    t.append("TableMeta: ").append(dsName).append(", ").append(tableName).append("\n");
    if (columnMetadata != null) {
      for (String key : columnMetadata.keySet()) {
        t.append(columnMetadata.get(key)).append(", ");
      }
    }

    return t.toString();
  }
}
