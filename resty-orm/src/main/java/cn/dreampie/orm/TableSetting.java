package cn.dreampie.orm;

import cn.dreampie.orm.generate.Generator;

import java.io.Serializable;

/**
 * Created by Dreampie on 15/11/29.
 */
public class TableSetting implements Serializable {

  private String tableName;
  private String generatedKey;
  private String[] primaryKey;
  private Generator generator;
  private boolean cached;
  private int expired;
  private String sequence;

  public TableSetting(String tableName) {
    this(tableName, null);
  }

  public TableSetting(String tableName, boolean cached) {
    this(tableName, cached, -1);
  }

  public TableSetting(String tableName, boolean cached, int expired) {
    this(tableName, null, cached, expired);
  }

  public TableSetting(String tableName, Generator generator, boolean cached, int expired) {
    this(tableName, null, null, generator, cached, expired);
  }

  public TableSetting(String tableName, String generatedKey, String[] primaryKey, boolean cached, int expired) {
    this(tableName, generatedKey, primaryKey, null, cached, expired);
  }

  public TableSetting(String tableName, String generatedKey) {
    this(tableName, generatedKey, (String[]) null);
  }

  public TableSetting(String tableName, String generatedKey, String[] primaryKey) {
    this(tableName, generatedKey, primaryKey, null);
  }

  public TableSetting(String tableName, String generatedKey, Generator generator) {
    this(tableName, generatedKey, null, generator, false, -1);
  }

  public TableSetting(String tableName, String generatedKey, String[] primaryKey, Generator generator) {
    this(tableName, generatedKey, primaryKey, generator, false, -1);
  }

  public TableSetting(String tableName, String generatedKey, String[] primaryKey, Generator generator, boolean cached, int expired) {
    this(tableName, generatedKey, primaryKey, generator, cached, expired, null);
  }

  public TableSetting(String tableName, String generatedKey, String[] primaryKey, Generator generator, boolean cached, int expired, String sequence) {
    this.tableName = tableName;
    if (generatedKey != null) {
      this.generatedKey = generatedKey;
    } else {
      this.generatedKey = Base.DEFAULT_GENERATED_KEY;
    }
    if (primaryKey != null) {
      this.primaryKey = primaryKey;
    } else {
      this.primaryKey = new String[]{};
    }
    this.generator = generator;
    this.cached = cached;
    this.expired = expired;
    this.sequence = sequence;
  }

  public String getTableName() {
    return tableName;
  }

  public TableSetting setTableName(String tableName) {
    this.tableName = tableName;
    return this;
  }

  public String getGeneratedKey() {
    return generatedKey;
  }

  public TableSetting setGeneratedKey(String generatedKey) {
    if (generatedKey != null) {
      this.generatedKey = generatedKey;
    } else {
      this.generatedKey = Base.DEFAULT_GENERATED_KEY;
    }
    return this;
  }

  public String[] getPrimaryKey() {
    return primaryKey;
  }

  public TableSetting setPrimaryKey(String[] primaryKey) {
    if (primaryKey != null) {
      this.primaryKey = primaryKey;
    } else {
      this.primaryKey = new String[]{};
    }
    return this;
  }

  public Generator getGenerator() {
    return generator;
  }

  public TableSetting setGenerator(Generator generator) {
    this.generator = generator;
    return this;
  }

  public boolean isCached() {
    return cached;
  }

  public TableSetting setCached(boolean cached) {
    this.cached = cached;
    return this;
  }

  public int getExpired() {
    return expired;
  }

  public TableSetting setExpired(int expired) {
    this.expired = expired;
    return this;
  }

  public String getSequence() {
    return sequence;
  }

  public TableSetting setSequence(String sequence) {
    this.sequence = sequence;
    return this;
  }
}
