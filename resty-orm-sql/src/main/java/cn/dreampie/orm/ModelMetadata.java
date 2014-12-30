/*
Copyright 2009-2014 Igor Polevoy

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License. 
*/


package cn.dreampie.orm;

import cn.dreampie.log.Logger;
import cn.dreampie.log.LoggerFactory;
import cn.dreampie.orm.annotation.Table;
import cn.dreampie.orm.dialect.Dialect;

import java.io.Serializable;
import java.util.Collections;
import java.util.SortedMap;

import static cn.dreampie.util.Checker.checkNotNull;


public class ModelMetadata implements Serializable {
  private final static Logger logger = LoggerFactory.getLogger(ModelMetadata.class);

  private SortedMap<String, ColumnMetadata> columnMetadata;
  private final String primaryKey;
  private final String tableName;
  private final Class<? extends Model> modelClass;
  private final boolean cached;
  private final ConnectionMetadata connectionMetadata;

  protected ModelMetadata(Class<? extends Model> modelClass, ConnectionMetadata connectionMetadata) {
    Table tableAnnotation = modelClass.getAnnotation(Table.class);
    checkNotNull(tableAnnotation, "Not found @Table Annotation.");
    this.modelClass = modelClass;
    this.primaryKey = tableAnnotation.primaryKey();
    this.tableName = tableAnnotation.name();
    this.cached = tableAnnotation.cached();
    this.connectionMetadata = connectionMetadata;
  }

  public String getDbName() {
    return connectionMetadata.getDbName();
  }

  public boolean cached() {
    return cached;
  }

  public Class<? extends Model> getModelClass() {
    return modelClass;
  }

  public String getTableName() {
    return tableName;
  }

  void setColumnMetadata(SortedMap<String, ColumnMetadata> columnMetadata) {
    this.columnMetadata = columnMetadata;
  }

  protected boolean tableExists() {
    return columnMetadata != null && columnMetadata.isEmpty();
  }


  public String getPrimaryKey() {
    return primaryKey;
  }


  public String getDbType() {
    return connectionMetadata.getDialect().getDbType();
  }

  public Dialect getDialect() {
    return connectionMetadata.getDialect();
  }

  /**
   * Provides column metadata map, keyed by attribute names.
   * Table columns correspond to ActiveJDBC model attributes.
   *
   * @return Provides column metadata map, keyed by attribute names.
   */
  public SortedMap<String, ColumnMetadata> getColumnMetadata() {
    checkNotNull(columnMetadata, "Failed to find table: " + getTableName());
    return Collections.unmodifiableSortedMap(columnMetadata);
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

  @Override
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
