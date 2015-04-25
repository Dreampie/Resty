package cn.dreampie.orm;

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
    this(tableName, false);
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
//  public Record(String tableName, String pKeys, boolean cached) {
//    this(tableName, pKeys, false, cached);
//  }

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
   * @param dsName    数据源名称
   * @param tableName 表名
   * @param cached    是否使用缓存
   */
  public Record(String dsName, String tableName, boolean cached) {
    this(dsName, tableName, DEFAULT_PRIMARY_KAY, cached);
  }

  /**
   * @param dsName    数据源名称
   * @param tableName 表名
   * @param pKeys     主键id，多主键使用逗号分割 自增主键放在第一位
   * @param cached    是否使用缓存
   */
  public Record(String dsName, String tableName, String pKeys, boolean cached) {
    this(dsName, tableName, pKeys, false, cached);
  }

  /**
   * @param dsName    数据源
   * @param tableName 表名
   * @param pKeys     主键id通过逗号拼接
   * @param lockKey   是否在更新的时候 要求必须使用全部主键
   * @param cached    使用对数据缓存
   */
  public Record(String dsName, String tableName, String pKeys, boolean lockKey, boolean cached) {
    checkNotNull(dsName, "Could not found dataSourceMeta.");
    checkNotNull(tableName, "Could not found tableName.");
    if (Metadata.hasTableMeta(dsName, tableName)) {
      this.tableMeta = Metadata.getTableMeta(dsName, tableName);
    } else {
      this.tableMeta = TableMetaBuilder.buildTableMeta(new TableMeta(dsName, tableName, pKeys, lockKey, cached), Metadata.getDataSourceMeta(dsName));
    }
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
      this.tableMeta = TableMetaBuilder.buildTableMeta(new TableMeta(useDS, tableMeta.getTableName(), tableMeta.getpKeys(), tableMeta.isLockKey(), tableMeta.isCached()), Metadata.getDataSourceMeta(useDS));
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
    return tableMeta;
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




