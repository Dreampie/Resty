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

  public Record(TableSetting tableSetting) {
    setTableMeta(tableSetting);
  }

  public Record(String dsName, TableSetting tableSetting) {
    setTableMeta(dsName, tableSetting);
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
      this.tableMeta = TableMetaBuilder.buildTableMeta(new TableMeta(useDS, new TableSetting(tableMeta.getTableName(), tableMeta.getGeneratedKey(), tableMeta.getPrimaryKey(), tableMeta.getGenerator(), tableMeta.isCached(), tableMeta.getExpired())), Metadata.getDataSourceMeta(useDS));
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

  public Record setTableMeta(TableSetting tableSetting) {
    return setTableMeta(null, tableSetting);
  }

  /**
   * 设置table信息
   *
   * @param dsName       数据源
   * @param tableSetting table设置
   * @return record
   */
  public Record setTableMeta(String dsName, TableSetting tableSetting) {
    if (dsName == null) {
      dsName = Metadata.getDefaultDsName();
    }
    String tableName = tableSetting.getTableName();
    checkNotNull(dsName, "Could not found dataSourceMeta.");
    checkNotNull(tableName, "Could not found tableName.");
    if (Metadata.hasTableMeta(dsName, tableName)) {
      this.tableMeta = Metadata.getTableMeta(dsName, tableName);
    } else {
      this.tableMeta = TableMetaBuilder.buildTableMeta(new TableMeta(dsName, tableSetting), Metadata.getDataSourceMeta(dsName));
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




