package cn.dreampie.orm.activerecord;

import cn.dreampie.orm.Metadata;
import cn.dreampie.orm.TableMeta;
import cn.dreampie.orm.TableMetaBuilder;
import cn.dreampie.common.generate.Generator;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

import static cn.dreampie.common.util.Checker.checkNotNull;

/**
 * Record
 */
public class Record extends Base<Record> {

  private TableMeta tableMeta;
  private boolean useCache = true;

  public Record() {
  }

  public Record(String tableName) {
    this(tableName, DEFAULT_GENERATED_KEY);
  }

  public Record(String tableName, boolean cached) {
    this(tableName, DEFAULT_GENERATED_KEY, null, new String[]{}, cached);
  }

  public Record(String tableName, String generatedKey) {
    this(null, tableName, generatedKey);
  }

  public Record(String tableName, String generatedKey, String[] primaryKey) {
    this(null, tableName, generatedKey, null, primaryKey);
  }

  public Record(String dsName, String tableName, String generatedKey) {
    this(dsName, tableName, generatedKey, new String[]{});
  }

  public Record(String dsName, String tableName, String generatedKey, String[] primaryKey) {
    this(dsName, tableName, generatedKey, null, primaryKey);
  }

  public Record(String tableName, String generatedKey, Generator generator) {
    this(null, tableName, generatedKey, generator);
  }

  public Record(String dsName, String tableName, String generatedKey, Generator generator) {
    this(dsName, tableName, generatedKey, generator, null);
  }

  public Record(String tableName, String generatedKey, Generator generator, String[] primaryKey) {
    this(null, tableName, generatedKey, generator, primaryKey);
  }

  public Record(String dsName, String tableName, String generatedKey, Generator generator, String[] primaryKey) {
    this(dsName, tableName, generatedKey, generator, primaryKey, false);
  }

  public Record(String tableName, String generatedKey, Generator generator, String[] primaryKey, boolean cached) {
    this(null, tableName, generatedKey, generator, primaryKey, cached);
  }

  /**
   * @param dsName       数据源
   * @param tableName    表名
   * @param generatedKey 数据库自动生成的主键
   * @param generator    主键生成器 默认uuid生成主键
   * @param primaryKey   其他主键
   * @param cached       使用对数据缓存
   */
  public Record(String dsName, String tableName, String generatedKey, Generator generator, String[] primaryKey, boolean cached) {
    setTableMeta(dsName, tableName, generatedKey, generator, primaryKey, cached);
  }

  /**
   * @param tableMeta 数据表的元数据
   */
  public Record(TableMeta tableMeta) {
    this.tableMeta = tableMeta;
  }

  public Record reNew() {
    return new Record(tableMeta);
  }

  private Record instance(String useDS, boolean useCache) {
    Record record;
    if (useDS != null && !tableMeta.getDsName().equals(useDS)) {
      record = new Record(tableMeta);
    } else {
      record = new Record(tableMeta);
    }
    record.useCache = useCache;
    return record;
  }

  /**
   * 是否使用缓存
   *
   * @return boolean
   */
  public boolean isUseCache() {
    return useCache;
  }

  /**
   * 不使用缓存
   *
   * @return Record
   */
  public Record unCache() {
    if (!this.useCache) {
      return this;
    } else {
      return instance(null, false);
    }
  }

  /**
   * 切换数据源
   *
   * @param useDS 数据源名称
   * @return Record
   */
  public Record useDS(String useDS) {
    checkNotNull(useDS, "DataSourceName could not be null.");
    if (!this.useCache && !tableMeta.getDsName().equals(useDS)) {
      this.tableMeta = TableMetaBuilder.buildTableMeta(new TableMeta(useDS, tableMeta.getTableName(), tableMeta.getId(), tableMeta.isGenerated(), tableMeta.getGenerator(), tableMeta.getPrimaryKey(), tableMeta.isCached()), Metadata.getDataSourceMeta(useDS));
      return this;
    } else {
      if (tableMeta.getDsName().equals(useDS)) {
        return this;
      } else {
        return instance(useDS, true);
      }
    }
  }

  /**
   * 获取数据表的元数据
   *
   * @return TableMeta
   */
  public TableMeta getTableMeta() {
    checkNotNull(tableMeta, "Could not find tableMeta.");
    return tableMeta;
  }

  public Record setTableMeta(String tableName) {
    setTableMeta(tableName, DEFAULT_GENERATED_KEY);
    return this;
  }

  public Record setTableMeta(String tableName, boolean cached) {
    setTableMeta(tableName, DEFAULT_GENERATED_KEY, null, new String[]{}, cached);
    return this;
  }

  public Record setTableMeta(String tableName, String generatedKey) {
    setTableMeta(null, tableName, generatedKey);
    return this;
  }

  public Record setTableMeta(String tableName, String generatedKey, String[] primaryKey) {
    setTableMeta(null, tableName, generatedKey, null, primaryKey);
    return this;
  }

  public Record setTableMeta(String dsName, String tableName, String generatedKey) {
    setTableMeta(dsName, tableName, generatedKey, null, null);
    return this;
  }

  public Record setTableMeta(String dsName, String tableName, String generatedKey, String[] primaryKey) {
    setTableMeta(dsName, tableName, generatedKey, null, primaryKey);
    return this;
  }

  public Record setTableMeta(String tableName, String generatedKey, Generator generator) {
    setTableMeta(null, tableName, generatedKey, generator);
    return this;
  }

  public Record setTableMeta(String dsName, String tableName, String generatedKey, Generator generator) {
    setTableMeta(dsName, tableName, generatedKey, generator, null);
    return this;
  }

  public Record setTableMeta(String tableName, String generatedKey, Generator generator, String[] primaryKey) {
    setTableMeta(null, tableName, generatedKey, generator, primaryKey);
    return this;
  }

  public Record setTableMeta(String dsName, String tableName, String generatedKey, Generator generator, String[] primaryKey) {
    setTableMeta(dsName, tableName, generatedKey, generator, primaryKey, false);
    return this;
  }

  public Record setTableMeta(String tableName, String generatedKey, Generator generator, String[] primaryKey, boolean cached) {
    setTableMeta(null, tableName, generatedKey, generator, primaryKey, cached);
    return this;
  }

  /**
   * 设置table信息
   *
   * @param dsName       数据源
   * @param tableName    表名
   * @param generatedKey 自动生成主键
   * @param generator    主键生成器
   * @param primaryKey   其他主键
   * @param cached       缓存
   * @return record
   */
  public Record setTableMeta(String dsName, String tableName, String generatedKey, Generator generator, String[] primaryKey, boolean cached) {
    if (primaryKey == null) {
      primaryKey = new String[]{};
    }
    if (dsName == null) {
      dsName = Metadata.getDefaultDsName();
    }
    checkNotNull(dsName, "Could not found dataSourceMeta.");
    checkNotNull(tableName, "Could not found tableName.");
    if (Metadata.hasTableMeta(dsName, tableName)) {
      this.tableMeta = Metadata.getTableMeta(dsName, tableName);
    } else {
      this.tableMeta = TableMetaBuilder.buildTableMeta(new TableMeta(dsName, tableName, generatedKey, generator != null, generator, primaryKey, cached), Metadata.getDataSourceMeta(dsName));
    }
    return this;
  }

  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    tableMeta = (TableMeta) in.readObject();
    putAttrs((Map<String, Object>) in.readObject());
    useCache = (Boolean) in.readObject();
    setAlias((String) in.readObject());
  }

  public void writeExternal(ObjectOutput out) throws IOException {
    //增加一个新的对象 String dsName, String tableName, String pKeys, boolean lockKey, boolean cached
    out.writeObject(tableMeta);
    out.writeObject(getAttrs());
    out.writeObject(useCache);
    out.writeObject(getAlias());
  }
}




