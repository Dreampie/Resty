package cn.dreampie.orm.meta;

import cn.dreampie.common.CaseStyle;
import cn.dreampie.common.entity.CaseInsensitiveMap;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;

import static cn.dreampie.common.util.Checker.checkNotNull;


public class EntityMeta implements Serializable {

  private final DataSourceMeta dataSourceMeta;
  private final String table;
  private final CaseStyle style;
  private final boolean cached;
  private final long version;
  private final IdMeta idMeta;
  private final Map<Field, FieldMeta> fieldMetas;
  private final SortedMap<String, Field> fields;
  private SortedMap<String, ColumnMeta> columnMetas;

  public EntityMeta(DataSourceMeta dataSourceMeta, String table, CaseStyle style, boolean cached,long version, IdMeta idMeta, Map<Field, FieldMeta> fieldMetas) {
    this.dataSourceMeta = dataSourceMeta;
    this.table = table;
    this.style = style;
    this.cached = cached;
    this.version=version;
    this.idMeta = idMeta;
    this.fieldMetas = fieldMetas;
    this.fields = new CaseInsensitiveMap<Field>();
    for (Map.Entry<Field, FieldMeta> fieldMetaEntry : fieldMetas.entrySet()) {
      fields.put(fieldMetaEntry.getValue().getColumn(), fieldMetaEntry.getKey());
    }
  }

  public DataSourceMeta getDataSourceMeta() {
    return dataSourceMeta;
  }

  public CaseStyle getStyle() {
    return style;
  }

  public String getDsName() {
    return getDataSourceMeta().getDsName();
  }

  public String getTable() {
    return table;
  }

  public boolean isCached() {
    return cached;
  }

  public long getVersion() {
    return version;
  }

  public IdMeta getIdMeta() {
    return idMeta;
  }

  protected boolean tableExists() {
    return columnMetas != null && columnMetas.isEmpty();
  }

  public SortedMap<String, Field> getFields() {
    return fields;
  }

  /**
   * fields
   *
   * @return fieldMap
   */
  public Map<Field, FieldMeta> getFieldMetas() {
    checkNotNull(fieldMetas, "Failed to found field: " + getTable());
    return Collections.unmodifiableMap(fieldMetas);
  }

  /**
   * Provides column metadata map, keyed by attribute table.
   * Entity columns correspond to ActiveJDBC model attributes.
   *
   * @return Provides column metadata map, keyed by attribute table.
   */
  public SortedMap<String, ColumnMeta> getColumnMetas() {
    checkNotNull(columnMetas, "Failed to found table: " + getTable());
    return Collections.unmodifiableSortedMap(columnMetas);
  }

  void setColumnMetas(SortedMap<String, ColumnMeta> columnMetas) {
    this.columnMetas = columnMetas;
  }

  public String getColumnTypeName(String columnName) {
    SortedMap<String, ColumnMeta> columnMetaSortedMap = getColumnMetas();
    return columnMetaSortedMap.get(columnName).getTypeName();
  }

  /**
   * returns true if this attribute is present in this meta model. This method i case insensitive.
   *
   * @param column attribute table, case insensitive.
   * @return true if this attribute is present in this meta model, false of not.
   */
  public boolean hasColumn(String column) {
    return columnMetas != null && columnMetas.containsKey(column);
  }

  /**
   * 返回列的类型
   *
   * @param column
   * @return
   */
  public Integer getDataType(String column) {
    if (hasColumn(column)) {
      return columnMetas.get(column).getDataType();
    }
    return null;
  }

  public String toString() {
    final StringBuilder t = new StringBuilder();
    t.append("EntityMeta: ").append(dataSourceMeta).append(", ").append(table).append("\n");
    if (columnMetas != null) {
      for (String key : columnMetas.keySet()) {
        t.append(columnMetas.get(key)).append(", ");
      }
    }

    return t.toString();
  }
}
