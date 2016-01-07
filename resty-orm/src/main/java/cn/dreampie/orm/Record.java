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
    this.tableMeta = getTableMeta(null);
  }

  public Record(TableSetting tableSetting) {
    this.tableMeta = getTableMeta(null, tableSetting);
  }

  public Record(String dsmName, TableSetting tableSetting) {
    this.tableMeta = getTableMeta(dsmName, tableSetting);
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

  private Record instance(String dsmName, boolean useCache) {
    Record record;
    if (dsmName != null && !tableMeta.getDsmName().equals(dsmName)) {
      record = new Record(getTableMeta(dsmName, tableMeta.getTableSetting()));
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
   * @param dsmName 数据源名称
   * @return Record
   */
  public Record useDSM(String dsmName) {
    checkNotNull(dsmName, "DataSourceMetaName could not be null.");
    //如果 useCache=false  表示已经实例化一个零时对象
    if (!this.useCache && !tableMeta.getDsmName().equals(dsmName)) {
      this.tableMeta = getTableMeta(dsmName, tableMeta.getTableSetting());
      return this;
    } else {
      if (tableMeta.getDsmName().equals(dsmName)) {
        return this;
      } else {
        return instance(dsmName, true);
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


  /**
   * 获取数据元
   *
   * @param dsmName
   * @return
   */
  private TableMeta getTableMeta(String dsmName) {
    return getTableMeta(dsmName, null);
  }

  /**
   * 获取表数据元
   *
   * @param dsmName
   * @param tableSetting
   * @return
   */
  private TableMeta getTableMeta(String dsmName, TableSetting tableSetting) {
    if (dsmName == null) {
      dsmName = Metadata.getDefaultDsmName();
    }

    if (tableSetting == null) {
      return new TableMeta(dsmName);
    } else {
      String tableName = tableSetting.getTableName();
      checkNotNull(dsmName, "Could not found dataSourceMeta.");
      checkNotNull(tableName, "Could not found tableName.");

      if (Metadata.hasTableMeta(dsmName, tableName)) {
        return Metadata.getTableMeta(dsmName, tableName);
      } else {
        return TableMetaBuilder.buildTableMeta(new TableMeta(dsmName, tableSetting), Metadata.getDataSourceMeta(dsmName));
      }
    }
  }

  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    tableMeta = (TableMeta) in.readObject();
    putAttrs((Map<String, Object>) in.readObject());
    useCache = (Boolean) in.readObject();
    setAlias((String) in.readObject());
  }

  public void writeExternal(ObjectOutput out) throws IOException {
    //增加一个新的对象 String dsmName, String tableName, String pKeys, boolean lockKey, boolean cached
    out.writeObject(tableMeta);
    out.writeObject(getAttrs());
    out.writeObject(useCache);
    out.writeObject(getAlias());
  }
}




