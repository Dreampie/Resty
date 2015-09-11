package cn.dreampie.orm;

import cn.dreampie.common.entity.exception.EntityException;

import static cn.dreampie.common.util.Checker.checkNotNull;

/**
 * Created by ice on 14-12-30.
 */
public abstract class Model<M extends Model> extends Base<M> {

  private boolean useCache = true;
  private String useDS = null;

  /**
   * 是否使用缓存
   *
   * @return boolean
   */
  public boolean isUseCache() {
    return useCache;
  }

  private M instance(String useDS, boolean useCache) {
    Model<M> instance = null;
    try {
      instance = getMClass().newInstance();
      instance.useDS = useDS;
      instance.useCache = useCache;
      instance.reSetAttrs(this.getAttrs()).reSetModifyAttrs(this.getModifyAttrs());
    } catch (InstantiationException e) {
      throw new EntityException(e.getMessage(), e);
    } catch (IllegalAccessException e) {
      throw new EntityException(e.getMessage(), e);
    }
    return (M) instance;
  }

  /**
   * 本次操作不使用缓存
   *
   * @return Model对象
   */
  public M unCache() {
    if (this.useDS != null) {
      this.useCache = false;
      return (M) this;
    } else {
      if (!this.useCache) {
        return (M) this;
      } else {
        return instance(null, false);
      }
    }
  }

  /**
   * 本次操作使用新数据源
   *
   * @param useDS 数据源名称
   * @return Model对象
   */
  public M useDS(String useDS) {
    checkNotNull(useDS, "DataSourceName could not be null.");
    if (!this.useCache) {
      this.useDS = useDS;
      return (M) this;
    } else {
      if (useDS.equals(this.useDS)) {
        return (M) this;
      } else {
        return instance(useDS, true);
      }
    }
  }


  /**
   * 是否需要在转换json的时候检测属性方法
   *
   * @return boolean
   */
  public boolean checkMethod() {
    return true;
  }

  /**
   * 获取当前实例数据表的元数据
   *
   * @return TableMeta
   */
  protected TableMeta getTableMeta() {
    TableMeta tableMeta = Metadata.getTableMeta(getMClass());
    if (useDS != null) {
      String tableName = tableMeta.getTableName();
      if (Metadata.hasTableMeta(useDS, tableName)) {
        tableMeta = Metadata.getTableMeta(useDS, tableName);
      } else {
        tableMeta = Metadata.addTableMeta(new TableMeta(useDS, tableName, tableMeta.getGeneratedKey(), tableMeta.isGenerated(), tableMeta.getGenerator(), tableMeta.getPrimaryKey(), tableMeta.isCached(),tableMeta.getExpired()));
      }
    }
    checkNotNull(tableMeta, "Could not find tableMeta.");
    return tableMeta;
  }

}
