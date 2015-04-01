package cn.dreampie.orm;

import cn.dreampie.common.entity.exception.EntityException;

import java.io.Serializable;

import static cn.dreampie.common.util.Checker.checkNotNull;

/**
 * Record
 */
public class Record extends Base<Record> implements Serializable {

  private DataSourceMeta dataSourceMeta;
  private TableMeta tableMeta;
  private boolean useCache = true;
  protected String alias;

  private Record() {
  }

  /**
   * @param tableName 表名
   * @param cached    是否启用缓存
   */
  public Record(String tableName, boolean cached) {
    this(tableName, Base.DEFAULT_PRIMARY_KAY, false, cached);
  }

  /**
   * @param tableName 表名
   * @param pKeys     主键id，多主键使用逗号分割 自增主键放在第一位
   * @param cached    是否使用缓存
   */
  public Record(String tableName, String pKeys, boolean cached) {
    this(tableName, pKeys, false, cached);
  }

  /**
   * @param tableName 表名
   * @param pKeys     主键id，多主键使用逗号分割 自增主键放在第一位
   * @param lockKey   更新操作是是否是要求使用全部主键条件
   * @param cached    是否使用缓存
   */
  public Record(String tableName, String pKeys, boolean lockKey, boolean cached) {
    this(Metadata.getDefaultDsName(), tableName, pKeys, lockKey, cached);
  }

  /**
   * 带DS结尾表示你要使用新的数据源
   *
   * @param dsName    数据源名称
   * @param tableName 表名
   * @param pKeys     主键id，多主键使用逗号分割 自增主键放在第一位
   * @param cached    是否使用缓存
   */
  public Record(String dsName, String tableName, String pKeys, boolean cached) {
    this(dsName, tableName, pKeys, false, cached);
  }

  /**
   * @param dsName    数据源名称
   * @param tableName 表名
   * @param pKeys     主键id，多主键使用逗号分割 自增主键放在第一位
   * @param lockKey   更新操作是是否是要求使用全部主键条件
   * @param cached    是否使用缓存
   */
  public Record(String dsName, String tableName, String pKeys, boolean lockKey, boolean cached) {
    this(Metadata.getDataSourceMeta(dsName), tableName, pKeys, lockKey, cached);
  }

  /**
   * @param dataSourceMeta 数据源
   * @param tableName      表名
   * @param pKeys          主键id通过逗号拼接
   * @param lockKey        是否在更新的时候 要求必须使用全部主键
   * @param cached         使用对数据缓存
   */
  public Record(DataSourceMeta dataSourceMeta, String tableName, String pKeys, boolean lockKey, boolean cached) {
    checkNotNull(dataSourceMeta, "Could not found dataSourceMeta.");
    checkNotNull(tableName, "Could not found tableName.");
    this.dataSourceMeta = dataSourceMeta;
    String dsName = dataSourceMeta.getDsName();
    if (Metadata.hasTableMeta(dsName, tableName)) {
      this.tableMeta = Metadata.getTableMeta(dsName, tableName);
    } else {
      this.tableMeta = TableMetaBuilder.buildModel(new TableMeta(dsName, tableName, pKeys, lockKey, cached), dataSourceMeta);
    }
  }

  /**
   * @param dataSourceMeta 数据源的元数据
   * @param tableMeta      数据表的元数据
   */
  public Record(DataSourceMeta dataSourceMeta, TableMeta tableMeta) {
    this.dataSourceMeta = dataSourceMeta;
    this.tableMeta = tableMeta;
  }

  public Record reNew() {
    return new Record(dataSourceMeta, tableMeta);
  }

  private Record instance(String useDS, boolean useCache) {
    Record record;
    if (useDS != null && !dataSourceMeta.getDsName().equals(useDS)) {
      record = new Record(Metadata.getDataSourceMeta(useDS), tableMeta);
    } else {
      record = new Record(dataSourceMeta, tableMeta);
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
    if (!this.useCache && !dataSourceMeta.getDsName().equals(useDS)) {
      this.dataSourceMeta = Metadata.getDataSourceMeta(useDS);
      return this;
    } else {
      if (dataSourceMeta.getDsName().equals(useDS)) {
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
    return tableMeta;
  }

  /**
   * 获取数据源的元数据
   *
   * @return DataSourceMeta
   */
  protected DataSourceMeta getDataSourceMeta() {
    return dataSourceMeta;
  }

  /**
   * 设置数据库查询别名
   *
   * @return String
   */
  public String getAlias() {
    return alias;
  }

  /**
   * 表的别名
   *
   * @param alias 别名
   * @return model
   */
  public Record setAlias(String alias) {
    if (this.alias != null)
      throw new EntityException("Model alias only set once.");
    this.alias = alias;
    return this;
  }

}




