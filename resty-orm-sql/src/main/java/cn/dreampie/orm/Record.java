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

  public boolean isUseCache() {
    return useCache;
  }

  private Record() {
  }

  public static Record use(String tableName) {
    return Record.use(tableName, false);
  }

  public static Record use(String tableName, boolean cached) {
    return Record.use(tableName, DS.DEFAULT_PRIMARY_KAY, false, cached);
  }

  public static Record use(String tableName, String pKeys) {
    return Record.use(tableName, pKeys, false);
  }

  public static Record use(String tableName, String pKeys, boolean lockKey) {
    return Record.use(tableName, pKeys, lockKey, false);
  }

  public static Record use(String tableName, String pKeys, boolean lockKey, boolean cached) {
    return Record.useDS(Metadata.getDefaultDsName(), tableName, pKeys, lockKey, cached);
  }

  /**
   * 带DS结尾表示你要使用新的数据源
   *
   * @param dsName    数据源名称
   * @param tableName 表名
   * @return Record执行对象
   */
  public static Record useDS(String dsName, String tableName) {
    return Record.useDS(dsName, tableName, false);
  }

  public static Record useDS(String dsName, String tableName, boolean cached) {
    return Record.useDS(dsName, tableName, DS.DEFAULT_PRIMARY_KAY, false, cached);
  }

  public static Record useDS(String dsName, String tableName, String pKeys) {
    return Record.useDS(dsName, tableName, pKeys, false);
  }

  public static Record useDS(String dsName, String tableName, String pKeys, boolean lockKey) {
    return Record.useDS(dsName, tableName, pKeys, lockKey, false);
  }

  public static Record useDS(String dsName, String tableName, String pKeys, boolean lockKey, boolean cached) {
    return Record.useDS(Metadata.getDataSourceMeta(dsName), tableName, pKeys, lockKey, cached);
  }

  /**
   * @param dataSourceMeta 数据源
   * @param tableName      表名
   * @param pKeys          主键id通过逗号拼接
   * @param lockKey        是否在更新的时候 要求必须使用全部主键
   * @param cached         使用对数据缓存
   * @return Record执行对象
   */
  public static Record useDS(DataSourceMeta dataSourceMeta, String tableName, String pKeys, boolean lockKey, boolean cached) {
    checkNotNull(dataSourceMeta, "Could not found dataSourceMeta.");
    checkNotNull(tableName, "Could not found tableName.");
    Record record = new Record();
    record.dataSourceMeta = dataSourceMeta;
    String dsName = dataSourceMeta.getDsName();
    if (Metadata.hasTableMeta(dsName, tableName)) {
      record.tableMeta = Metadata.getTableMeta(dsName, tableName);
    } else {
      record.tableMeta = TableMetaBuilder.buildModel(new TableMeta(dsName, tableName, pKeys, lockKey, cached), dataSourceMeta);
    }
    return record;
  }

  public static Record useDS(DataSourceMeta dataSourceMeta, TableMeta tableMeta) {
    Record record = new Record();
    record.dataSourceMeta = dataSourceMeta;
    record.tableMeta = tableMeta;
    return record;
  }

  /**
   * create new record
   *
   * @return Record
   */
  public Record reNew() {
    return Record.useDS(dataSourceMeta, tableMeta);
  }

  private Record instance(String useDS, boolean useCache) {
    Record record = reNew();
    if (useDS != null && !dataSourceMeta.getDsName().equals(useDS)) {
      record.dataSourceMeta = Metadata.getDataSourceMeta(useDS);
    }
    record.useCache = useCache;
    return record;
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




