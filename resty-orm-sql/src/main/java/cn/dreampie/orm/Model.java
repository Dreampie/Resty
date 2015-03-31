package cn.dreampie.orm;

import cn.dreampie.common.entity.CaseInsensitiveMap;
import cn.dreampie.orm.exception.ModelException;

import java.io.Serializable;
import java.util.Map;

import static cn.dreampie.common.util.Checker.checkNotNull;

/**
 * Created by ice on 14-12-30.
 */
public abstract class Model<M extends Model> extends Base<M> implements Serializable {

  private boolean useCache = true;
  private String useDS = null;
  private String alias;

  public boolean isUseCache() {
    return useCache;
  }

  private M instance(String useDS, boolean useCache) {
    M instance = null;
    try {
      instance = (M) getClass().newInstance();
      instance.useDS = useDS;
      instance.useCache = useCache;
    } catch (InstantiationException e) {
      throw new ModelException(e);
    } catch (IllegalAccessException e) {
      throw new ModelException(e);
    }
    return instance;
  }

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

  public M useDS(String useDS) {
    checkNotNull(useDS, "DataSourceName could not be null.");
    if (!this.useCache) {
      this.useDS = useDS;
      return (M) this;
    } else {
      if (this.useDS.equals(useDS)) {
        return (M) this;
      } else {
        return instance(useDS, true);
      }
    }
  }

  /**
   * Attributes of this model
   */
  private Map<String, Object> attrs = new CaseInsensitiveMap<Object>();


  /**
   * Return attribute Map.
   * <p/>
   * Danger! The update method will ignore the attribute if you change it directly.
   * You must use set method to change attribute that update method can handle it.
   */
  public Map<String, Object> getAttrs() {
    return attrs;
  }

  /**
   * Flag of column has been modified. update need this flag
   */
  private Map<String, Object> modifyAttrs = new CaseInsensitiveMap<Object>();

  /**
   * 获取已经修改了的属性
   *
   * @return Map<String, Object>
   */
  public Map<String, Object> getModifyAttrs() {
    return modifyAttrs;
  }

  /**
   * 表的别名
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
  public M setAlias(String alias) {
    if (this.alias != null)
      throw new ModelException("Model alias only set once.");
    this.alias = alias;
    return (M) this;
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
    TableMeta tableMeta = Metadata.getTableMeta(getClass());
    if (useDS != null) {
      String tableName = tableMeta.getTableName();
      if (Metadata.hasTableMeta(useDS, tableName)) {
        tableMeta = Metadata.getTableMeta(useDS, tableName);
      } else {
        tableMeta = Metadata.addTableMeta(new TableMeta(useDS, tableName, tableMeta.getpKeys(), tableMeta.isLockKey(), tableMeta.isCached()));
      }
    }
    return tableMeta;
  }


  /**
   * 获取数据源元数据
   *
   * @return DataSourceMeta
   */
  protected DataSourceMeta getDataSourceMeta() {
    return Metadata.getDataSourceMeta(getTableMeta().getDsName());
  }

}
