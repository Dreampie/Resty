package cn.dreampie.orm;

import cn.dreampie.common.Constant;
import cn.dreampie.common.entity.CaseInsensitiveMap;
import cn.dreampie.common.entity.Entity;
import cn.dreampie.common.util.Joiner;
import cn.dreampie.log.Logger;
import cn.dreampie.orm.cache.QueryCache;
import cn.dreampie.orm.dialect.Dialect;
import cn.dreampie.orm.exception.DBException;
import cn.dreampie.orm.exception.ModelException;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static cn.dreampie.common.util.Checker.checkArgument;
import static cn.dreampie.common.util.Checker.checkNotNull;

/**
 * Created by ice on 14-12-30.
 */
public abstract class Base<M extends Base> extends Entity<Base> implements Serializable {

  private static final Logger logger = Logger.getLogger(Base.class);

  private static final boolean devMode = Constant.devMode;
  private boolean inCache = true;
  /**
   * Attributes of this model
   */
  private Map<String, Object> attrs = new CaseInsensitiveMap<Object>();

  public M inCache(boolean inCache) {
    this.inCache = inCache;
    return (M) this;
  }

  protected <T> T getCache(String sql, Object[] paras) {
    TableMeta tableMeta = getTableMeta();
    if (tableMeta.isCached()) {
      return (T) QueryCache.instance().get(tableMeta.getDsName(), tableMeta.getTableName(), sql, paras);
    }
    return null;
  }

  protected void addCache(String sql, Object[] paras, Object cache) {
    TableMeta tableMeta = getTableMeta();
    if (tableMeta.isCached()) {
      QueryCache.instance().add(tableMeta.getDsName(), tableMeta.getTableName(), sql, paras, cache);
    }
  }

  protected void purgeCache() {
    TableMeta tableMeta = getTableMeta();
    if (tableMeta.isCached()) {
      QueryCache.instance().purge(tableMeta.getDsName(), tableMeta.getTableName());
    }
  }

  /**
   * Flag of column has been modified. update need this flag
   */
  private Map<String, Object> modifyAttrs = new CaseInsensitiveMap<Object>();
  ;

  public Map<String, Object> getModifyAttrs() {
    return modifyAttrs;
  }

  /**
   * Return attribute name of this model.
   */
  public String[] getModifyAttrNames() {
    Set<String> attrNameSet = modifyAttrs.keySet();
    return attrNameSet.toArray(new String[attrNameSet.size()]);
  }

  /**
   * Return attribute values of this model.
   */
  public Object[] getModifyAttrValues() {
    java.util.Collection<Object> attrValueCollection = modifyAttrs.values();
    return attrValueCollection.toArray(new Object[attrValueCollection.size()]);
  }

  protected TableMeta getTableMeta() {
    return Metadata.getModelTableMeta(getClass());
  }

  protected DataSourceMeta getDataSourceMeta() {
    return Metadata.getDataSourceMeta(getTableMeta().getDsName());
  }


  protected Dialect getDialect() {
    return getDataSourceMeta().getDialect();
  }

  /**
   * Set attribute to model.
   *
   * @param attr  the attribute name of the model
   * @param value the value of the attribute
   * @return this model
   * @throws cn.dreampie.orm.exception.DBException if the attribute is not exists of the model
   */
  public M set(String attr, Object value) {
    if (getTableMeta().hasAttribute(attr)) {
      attrs.put(attr, value);
      modifyAttrs.put(attr, value);  // Add modify flag, update() need this flag.
      return (M) this;
    }
    throw new DBException("The attribute name is not exists: " + attr);
  }

  /**
   * Put key value pair to the model when the key is not attribute of the model.
   */
  public M put(String key, Object value) {
    if (getTableMeta().hasAttribute(key))
      modifyAttrs.put(key, value);
    attrs.put(key, value);
    return (M) this;
  }

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
   * Check the table name. The table name must in sql.
   */
  private void checkTableName(String tableName, String sql) {
    if (!sql.toLowerCase().contains(tableName.toLowerCase()))
      throw new DBException("The table name: " + tableName + " not in your sql.");
  }

  /**
   * Get id after save method.
   */
  private void getGeneratedKey(PreparedStatement pst, String pKey) throws SQLException {
    if (get(pKey) == null) {
      ResultSet rs = pst.getGeneratedKeys();
      if (rs.next()) {
        set(pKey, rs.getObject(1));    // It returns Long object for int colType
        rs.close();
      }
    }
  }

  private void getGeneratedKey(PreparedStatement pst, String pKey, List<M> models) throws SQLException {
    ResultSet rs = pst.getGeneratedKeys();
    for (M model : models) {
      if (model.get(pKey) == null) {
        if (rs.next()) {
          model.set(pKey, rs.getObject(1));
        }
      }
    }
    rs.close();
  }

  /**
   * Find model.
   *
   * @param sql   an SQL statement that may contain one or more '?' IN parameter placeholders
   * @param paras the parameters of sql
   * @return the list of Model
   */
  public List<M> find(String sql, Object... paras) {
    List<M> result = null;
    boolean cached = false;
    if (inCache) {
      cached = getTableMeta().isCached();
      //hit cache
      if (cached) {
        result = getCache(sql, paras);
      }
      if (result != null) {
        return result;
      }
    } else {
      inCache = true;
    }
    if (devMode)
      checkTableName(getTableMeta().getTableName(), sql);

    DataSourceMeta dsm = getDataSourceMeta();
    Connection conn = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    try {
      conn = dsm.getConnection();
      pst = DS.getPreparedStatement(conn, getTableMeta().getPrimaryKey(), sql, paras);
      rs = pst.executeQuery();
      result = ModelBuilder.build(rs, getClass());
    } catch (SQLException e) {
      throw new DBException(e.getMessage(), e);
    } catch (InstantiationException e) {
      throw new ModelException(e.getMessage(), e);
    } catch (IllegalAccessException e) {
      throw new ModelException(e.getMessage(), e);
    } finally {
      dsm.close(rs, pst, conn);
    }
    //add cache
    if (cached) {
      addCache(sql, paras, result);
    }
    return result;
  }


  /**
   * @see #find(String, Object...)
   */
  public List<M> find(String sql) {
    return find(sql, DS.NULL_PARA_ARRAY);
  }

  /**
   * Find first model. I recommend add "limit 1" in your sql.
   *
   * @param sql   an SQL statement that may contain one or more '?' IN parameter placeholders
   * @param paras the parameters of sql
   * @return Model
   */
  public M findFirst(String sql, Object... paras) {
    List<M> result = find(sql, paras);
    return result.size() > 0 ? result.get(0) : null;
  }

  /**
   * @param sql an SQL statement
   * @see #findFirst(String, Object...)
   */
  public M findFirst(String sql) {
    List<M> result = find(sql, DS.NULL_PARA_ARRAY);
    return result.size() > 0 ? result.get(0) : null;
  }

  /**
   * Find model by id.
   *
   * @param id the id value of the model
   */
  public M findById(Object id) {
    return findColsById("*", id);
  }

  public M findByIds(Object... ids) {
    return findColsByIds("*", ids);
  }

  /**
   * Find model by id. Fetch the specific columns only.
   * Example: User user = User.dao.findById(15, "name, age");
   *
   * @param columns the specific columns
   * @param id      the id value of the model
   */
  public M findColsById(String columns, Object id) {
    String sql = getDialect().select(getTableMeta().getTableName(), "", getTableMeta().getPrimaryKey() + "=?", columns.split(","));
    List<M> result = find(sql, id);
    return result.size() > 0 ? result.get(0) : null;
  }

  public M findColsByIds(String columns, Object... ids) {
    String sql = getDialect().select(getTableMeta().getTableName(), "", Joiner.on("=? AND ").join(getTableMeta().getPrimaryKeys()) + "=?", columns.split(","));
    List<M> result = find(sql, ids);
    return result.size() > 0 ? result.get(0) : null;
  }

  /**
   * @param pageNumber 页码
   * @param pageSize   每页数量
   * @param sql        sql语句
   * @param paras      参数
   * @return
   */
  public Page<M> paginate(int pageNumber, int pageSize, String sql, Object... paras) {
    checkArgument(pageNumber >= 1 && pageSize >= 1, "pageNumber and pageSize must be more than 0");

    DataSourceMeta dsm = getDataSourceMeta();
    Dialect dialect = dsm.getDialect();

    long totalRow = 0;
    int totalPage = 0;
    List result = DS.use(getDataSourceMeta().getDsName()).query(dialect.countWith(sql), paras);
    int size = result.size();
    if (size == 1)
      totalRow = ((Number) result.get(0)).longValue();    // totalRow = (Long)result.get(0);
    else if (size > 1)
      totalRow = result.size();
    else
      return new Page<M>(new ArrayList<M>(0), pageNumber, pageSize, 0, 0);  // totalRow = 0;

    totalPage = (int) (totalRow / pageSize);
    if (totalRow % pageSize != 0) {
      totalPage++;
    }

    // --------
    List<M> list = find(dialect.paginateWith(pageNumber, pageSize, sql), paras);
    return new Page<M>(list, pageNumber, pageSize, totalPage, (int) totalRow);
  }

  /**
   * @see #paginate(int, int, String, Object...)
   */
  public Page<M> paginate(int pageNumber, int pageSize, String sql) {
    return paginate(pageNumber, pageSize, sql, DS.NULL_PARA_ARRAY);
  }

  /**
   * Save model.
   */
  public boolean save() {
    //清除缓存
    if (getTableMeta().isCached()) {
      purgeCache();
    }

    DataSourceMeta dsm = getDataSourceMeta();
    Dialect dialect = dsm.getDialect();
    TableMeta tableMeta = getTableMeta();

    String sql = dialect.insert(tableMeta.getTableName(), getModifyAttrNames());

    // --------
    Connection conn = null;
    PreparedStatement pst = null;
    int result = 0;
    try {
      conn = dsm.getConnection();
      pst = DS.getPreparedStatement(conn, getTableMeta().getPrimaryKey(), sql, getModifyAttrValues());

      result = pst.executeUpdate();
      getGeneratedKey(pst, tableMeta.getPrimaryKey());
      modifyAttrs.clear();
      return result >= 1;
    } catch (SQLException e) {
      throw new DBException(e.getMessage(), e);
    } finally {
      dsm.close(pst, conn);
    }
  }

  public boolean save(M... models) {
    return save(Arrays.asList(models));
  }

  /**
   * 批量保存model
   *
   * @param models model集合
   * @return
   */
  public boolean save(List<M> models) {
    if (models == null || models.size() <= 0) {
      logger.warn("Cloud not found models to save.");
      return false;
    }

    M firstModel = models.get(0);
    if (models.size() == 1) {
      return firstModel.save();
    }

    //清除models缓存
    if (firstModel.getTableMeta().isCached()) {
      firstModel.purgeCache();
    }

    DataSourceMeta dsm = firstModel.getDataSourceMeta();
    Dialect dialect = dsm.getDialect();
    TableMeta tableMeta = firstModel.getTableMeta();

    String[] columns = firstModel.getModifyAttrNames();
    String sql = dialect.insert(tableMeta.getTableName(), columns);

    //参数
    Object[][] paras = new Object[models.size()][columns.length];

    for (int i = 0; i < paras.length; i++) {
      for (int j = 0; j < paras[i].length; j++) {
        paras[i][j] = models.get(i).get(columns[j]);
      }
    }

    // --------
    Connection conn = null;
    PreparedStatement pst = null;
    int[] result = null;
    Boolean autoCommit = null;
    try {
      conn = dsm.getConnection();
      autoCommit = conn.getAutoCommit();
      if (autoCommit)
        conn.setAutoCommit(false);

      pst = DS.getPreparedStatement(conn, getTableMeta().getPrimaryKey(), sql, paras);
      result = pst.executeBatch();
      getGeneratedKey(pst, tableMeta.getPrimaryKey(), models);
      //没有事务的情况下 手动提交
      if (dsm.getCurrentConnection() == null)
        conn.commit();
      conn.setAutoCommit(autoCommit);
      for (M model : models) {
        model.getModifyAttrs().clear();
      }

      for (int r : result) {
        if (r < 1) {
          return false;
        }
      }
      return true;
    } catch (SQLException e) {
      throw new DBException(e.getMessage(), e);
    } finally {
      dsm.close(pst, conn);
    }
  }


  //update  base
  public int update(String sql, Object... paras) {
    //清除缓存
    if (getTableMeta().isCached()) {
      purgeCache();
    }
    if (devMode)
      checkTableName(getTableMeta().getTableName(), sql);
    return DS.use(getTableMeta().getDsName()).update(sql, paras);
  }

  /**
   * Delete model.
   */
  public boolean delete() {
    TableMeta tableMeta = getTableMeta();

    Object id = attrs.get(tableMeta.getPrimaryKey());
    checkNotNull(id, "You can't delete model without primaryKey " + tableMeta.getPrimaryKey() + ".");

    //锁定主键 删除的时候 使用所有主键作为条件
    if (tableMeta.isLockKey()) {
      String[] pkeys = tableMeta.getPrimaryKeys();
      Object[] ids = new Object[pkeys.length];
      ids[0] = id;
      int i = 1;
      for (String idKey : pkeys) {
        ids[i] = attrs.get(idKey);
        i++;
      }
      return deleteByIds(ids);
    } else {
      return deleteById(id);
    }
  }

  /**
   * Delete model by id.
   *
   * @param id the id value of the model
   * @return true if delete succeed otherwise false
   */
  public boolean deleteById(Object id) {
    checkNotNull(id, "You can't delete model without primaryKey.");
    TableMeta tableMeta = getTableMeta();
    String sql = getDialect().delete(tableMeta.getTableName(), tableMeta.getPrimaryKey() + "=?");
    int result = update(sql, id);
    return result > 0;
  }

  public boolean deleteByIds(Object... ids) {
    checkNotNull(ids, "You can't delete model without primaryKey.");
    TableMeta tableMeta = getTableMeta();
    String sql = getDialect().delete(tableMeta.getTableName(), Joiner.on("=? AND ").join(tableMeta.getPrimaryKeys()) + "=?");
    int result = update(sql, ids);
    return result > 0;
  }

  /**
   * Update model.
   */
  public boolean update() {
    if (modifyAttrs.isEmpty())
      return false;

    TableMeta tableMeta = getTableMeta();
    Dialect dialect = getDialect();

    String pKey = tableMeta.getPrimaryKey();
    Object id = attrs.get(pKey);
    checkNotNull(id, "You can't update model without Primary Key " + pKey + ".");

    String where = null;
    Object[] paras = null;
    Object[] modifys = getModifyAttrValues();
    //锁定主键 更新的时候 使用所有主键作为条件
    if (tableMeta.isLockKey()) {
      String[] pkeys = tableMeta.getPrimaryKeys();
      Object[] ids = new Object[pkeys.length];
      ids[0] = id;
      int i = 1;
      for (String idKey : pkeys) {
        ids[i] = attrs.get(idKey);
        i++;
      }
      paras = new Object[ids.length + modifys.length];
      System.arraycopy(modifys, 0, paras, 0, modifys.length);
      System.arraycopy(ids, 0, paras, modifys.length, ids.length);
      where = Joiner.on("=?,").join(tableMeta.getPrimaryKeys());
    } else {
      paras = new Object[1 + modifys.length];
      System.arraycopy(modifys, 0, paras, 0, modifys.length);
      paras[modifys.length] = id;
      where = pKey;
    }
    String[] modifyNames = getModifyAttrNames();
    String sql = dialect.update(tableMeta.getTableName(), "", where + "=?", modifyNames);

    if (modifyNames.length <= 0) {  // Needn't update
      return false;
    }

    int result = update(sql, paras);
    if (result >= 1) {
      modifyAttrs.clear();
      return true;
    }
    return false;
  }

  /**
   * Set attributes with other model.
   *
   * @param model the Model
   * @return this Model
   */
  public M setAttrs(M model) {
    return (M) setAttrs(model.getAttrs());
  }

  /**
   * Set attributes with Map.
   *
   * @param attrs attributes of this model
   * @return this Model
   */
  public M setAttrs(Map<String, Object> attrs) {
    for (Map.Entry<String, Object> e : attrs.entrySet())
      set(e.getKey(), e.getValue());
    return (M) this;
  }


  public M putAttrs(Map<String, Object> attrs) {
    for (Map.Entry<String, Object> e : attrs.entrySet())
      put(e.getKey(), e.getValue());
    return (M) this;
  }

  /**
   * Remove attribute of this model.
   *
   * @param attr the attribute name of the model
   * @return this model
   */
  public M remove(String attr) {
    attrs.remove(attr);
    modifyAttrs.remove(attr);
    return (M) this;
  }

  /**
   * Remove attributes of this model.
   *
   * @param attrs the attribute name of the model
   * @return this model
   */
  public M remove(String... attrs) {
    if (attrs != null)
      for (String a : attrs) {
        this.attrs.remove(a);
        this.modifyAttrs.remove(a);
      }
    return (M) this;
  }

  /**
   * Remove attributes if it is null.
   *
   * @return this model
   */
  public M removeNull() {
    for (Iterator<Map.Entry<String, Object>> it = attrs.entrySet().iterator(); it.hasNext(); ) {
      Map.Entry<String, Object> e = it.next();
      if (e.getValue() == null) {
        it.remove();
        modifyAttrs.remove(e.getKey());
      }
    }
    return (M) this;
  }

  /**
   * Keep attributes of this model and remove other attributes.
   *
   * @param attrs the attribute name of the model
   * @return this model
   */
  public M keep(String... attrs) {
    if (attrs != null && attrs.length > 0) {
      Map<String, Object> newAttrs = new CaseInsensitiveMap<Object>();  // new HashMap<String, Object>(attrs.length);
      Map<String, Object> newModifyFlag = new CaseInsensitiveMap<Object>(); // new HashSet<String>();
      for (String a : attrs) {
        if (this.attrs.containsKey(a))  // prevent put null value to the newColumns
          newAttrs.put(a, this.attrs.get(a));
        if (this.modifyAttrs.containsKey(a))
          newModifyFlag.put(a, this.attrs.get(a));
      }
      this.attrs = newAttrs;
      this.modifyAttrs = newModifyFlag;
    } else {
      this.attrs.clear();
      this.modifyAttrs.clear();
    }
    return (M) this;
  }

  /**
   * Keep attribute of this model and remove other attributes.
   *
   * @param attr the attribute name of the model
   * @return this model
   */
  public M keep(String attr) {
    if (attrs.containsKey(attr)) {  // prevent put null value to the newColumns
      Object keepIt = attrs.get(attr);
      boolean keepFlag = modifyAttrs.containsKey(attr);
      attrs.clear();
      modifyAttrs.clear();
      attrs.put(attr, keepIt);
      if (keepFlag)
        modifyAttrs.put(attr, keepIt);
    } else {
      attrs.clear();
      modifyAttrs.clear();
    }
    return (M) this;
  }

  /**
   * Remove all attributes of this model.
   *
   * @return this model
   */
  public M clear() {
    attrs.clear();
    modifyAttrs.clear();
    return (M) this;
  }

  public int hashCode() {
    return (attrs == null ? 0 : attrs.hashCode()) ^ (modifyAttrs == null ? 0 : modifyAttrs.hashCode());
  }
}
