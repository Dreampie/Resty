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
public abstract class Model<M extends Model> extends Entity<Model> implements Serializable {

  private static final Logger logger = Logger.getLogger(Model.class);

  private static final boolean devMode = Constant.devMode;
  private boolean useCache = true;
  private String useDS = null;

  /**
   * Attributes of this model
   */
  private Map<String, Object> attrs = new CaseInsensitiveMap<Object>();

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

  protected <T> T getCache(String sql, Object[] paras) {
    TableMeta tableMeta = getTableMeta();
    if (tableMeta.isCached()) {
      return (T) QueryCache.instance().get("Model", tableMeta.getDsName(), tableMeta.getTableName(), sql, paras);
    }
    return null;
  }

  protected void addCache(String sql, Object[] paras, Object cache) {
    TableMeta tableMeta = getTableMeta();
    if (tableMeta.isCached()) {
      QueryCache.instance().add("Model", tableMeta.getDsName(), tableMeta.getTableName(), sql, paras, cache);
    }
  }

  protected void purgeCache() {
    TableMeta tableMeta = getTableMeta();
    if (tableMeta.isCached()) {
      QueryCache.instance().purge("Model", tableMeta.getDsName(), tableMeta.getTableName());
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

    TableMeta tableMeta = getTableMeta();
    if (useCache) {
      cached = tableMeta.isCached();
      //hit cache
      if (cached) {
        result = getCache(sql, paras);
      }
      if (result != null) {
        return result;
      }
    } else {
      logger.debug("This query not use cache.");
      useCache = true;
    }
    if (devMode)
      checkTableName(tableMeta.getTableName(), sql);

    DataSourceMeta dsm = getDataSourceMeta();
    Connection conn = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    try {
      conn = dsm.getConnection();
      pst = DS.getPreparedStatement(conn, tableMeta.getPrimaryKey(), sql, paras);
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
    TableMeta tableMeta = getTableMeta();
    String sql = getDialect().select(tableMeta.getTableName(), "", tableMeta.getPrimaryKey() + "=?", columns.split(","));
    List<M> result = find(sql, id);
    return result.size() > 0 ? result.get(0) : null;
  }

  public M findColsByIds(String columns, Object... ids) {
    TableMeta tableMeta = getTableMeta();
    String sql = getDialect().select(tableMeta.getTableName(), "", Joiner.on("=? AND ").join(tableMeta.getPrimaryKeys()) + "=?", columns.split(","));
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
    TableMeta tableMeta = getTableMeta();
    Dialect dialect = dsm.getDialect();

    long totalRow = 0;
    int totalPage = 0;

    boolean cached = false;
    List result = null;
    if (useCache) {
      cached = tableMeta.isCached();
      //hit cache
      if (cached) {
        result = getCache(sql, paras);
      }
    } else {
      logger.debug("This query not use cache.");
      useCache = true;
    }

    if (result == null) {
      result = DS.useDS(dsm.getDsName()).query(dialect.countWith(sql), paras);
      //add cache
      if (cached) {
        addCache(sql, paras, result);
      }
    }
    int size = result.size();
    if (size == 1)
      totalRow = ((Number) result.get(0)).longValue();
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
    TableMeta tableMeta = getTableMeta();
    //清除缓存
    if (tableMeta.isCached()) {
      purgeCache();
    }

    DataSourceMeta dsm = getDataSourceMeta();
    Dialect dialect = dsm.getDialect();

    String sql = dialect.insert(tableMeta.getTableName(), getModifyAttrNames());

    // --------
    Connection conn = null;
    PreparedStatement pst = null;
    int result = 0;
    try {
      conn = dsm.getConnection();
      pst = DS.getPreparedStatement(conn, tableMeta.getPrimaryKey(), sql, getModifyAttrValues());

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
    TableMeta tableMeta = firstModel.getTableMeta();
    //清除models缓存
    if (tableMeta.isCached()) {
      firstModel.purgeCache();
    }

    DataSourceMeta dsm = firstModel.getDataSourceMeta();
    Dialect dialect = dsm.getDialect();

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

      pst = DS.getPreparedStatement(conn, tableMeta.getPrimaryKey(), sql, paras);
      result = pst.executeBatch();
      getGeneratedKey(pst, tableMeta.getPrimaryKey(), models);
      //没有事务的情况下 手动提交
      if (dsm.getCurrentConnection() == null)
        conn.commit();
      conn.setAutoCommit(autoCommit);
      for (M model : models) {
        model.getModifyAttrs().clear();
      }
      //判断是否是保存了所有数据
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
  protected boolean update(String sql, Object... paras) {
    TableMeta tableMeta = getTableMeta();
    //清除缓存
    if (tableMeta.isCached()) {
      purgeCache();
    }
    if (devMode)
      checkTableName(tableMeta.getTableName(), sql);
    int result = DS.useDS(tableMeta.getDsName()).update(sql, paras);
    return result > 0;
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
    return update(sql, id);
  }

  public boolean deleteByIds(Object... ids) {
    checkNotNull(ids, "You can't delete model without primaryKey.");
    TableMeta tableMeta = getTableMeta();
    String sql = getDialect().delete(tableMeta.getTableName(), Joiner.on("=? AND ").join(tableMeta.getPrimaryKeys()) + "=?");
    return update(sql, ids);
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

    boolean result = update(sql, paras);
    if (result) {
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


  //////////////From Model////////////////////

  protected String alias;

  /**
   * 查询全部的model数据
   *
   * @return model 集合
   */
  public List<M> findAll() {
    return find(getDialect().select(getTableMeta().getTableName()));
  }

  /**
   * 查询全部的model数据
   *
   * @param columns 列 用逗号分割
   * @return model 集合
   */
  public List<M> findColsAll(String columns) {
    return find(getDialect().select(getTableMeta().getTableName(), columns.split(",")));
  }

  /**
   * 根据where条件查询model集合
   *
   * @param where 条件
   * @param paras 参数
   * @return list
   */
  public List<M> findBy(String where, Object... paras) {
    return find(getDialect().select(getTableMeta().getTableName(), getAlias(), where), paras);
  }

  /**
   * 根据where条件查询model集合
   *
   * @param colums 列 用逗号分割
   * @param where  条件
   * @param paras  参数
   * @return model集合
   */
  public List<M> findColsBy(String colums, String where, Object... paras) {
    return find(getDialect().select(getTableMeta().getTableName(), getAlias(), where, colums.split(",")), paras);
  }

  /**
   * 根据条件查询 前几位
   *
   * @param topNumber 前几位
   * @param where     条件
   * @param paras     参数
   * @return list
   */
  public List<M> findTopBy(int topNumber, String where, Object... paras) {
    return paginate(1, topNumber, getDialect().select(getTableMeta().getTableName(), getAlias(), where), paras).getList();
  }

  /**
   * 根据条件查询 前几位
   *
   * @param topNumber 前几位
   * @param columns   列 用逗号分割
   * @param where     条件
   * @param paras     参数
   * @return list
   */
  public List<M> findColsTopBy(int topNumber, String columns, String where, Object... paras) {
    return paginate(1, topNumber, getDialect().select(getTableMeta().getTableName(), getAlias(), where, columns.split(",")), paras).getList();
  }

  /**
   * 根据条件查询第一个对象
   *
   * @param where 条件
   * @param paras 参数
   * @return model对象
   */
  public M findFirstBy(String where, Object... paras) {
    return findFirst(getDialect().select(getTableMeta().getTableName(), getAlias(), where), paras);
  }

  /**
   * 根据条件查询第一个对象
   *
   * @param columns 列 用逗号分割
   * @param where   条件
   * @param paras   参数
   * @return model对象
   */
  public M findColsFirstBy(String columns, String where, Object... paras) {
    return findFirst(getDialect().select(getTableMeta().getTableName(), getAlias(), where, columns.split(",")), paras);
  }

  /**
   * 分页查询
   *
   * @param pageNumber 页码
   * @param pageSize   每页大小
   * @return 分页对象
   */
  public Page<M> paginateAll(int pageNumber, int pageSize) {
    return paginate(pageNumber, pageSize, getDialect().select(getTableMeta().getTableName()));
  }

  /**
   * 分页查询
   *
   * @param pageNumber 页码
   * @param pageSize   每页大小
   * @param columns    列 用逗号分割
   * @return 分页对象
   */
  public Page<M> paginateColsAll(int pageNumber, int pageSize, String columns) {
    return paginate(pageNumber, pageSize, getDialect().select(getTableMeta().getTableName(), columns.split(",")));
  }

  /**
   * 分页查询
   *
   * @param pageNumber 页码
   * @param pageSize   每页大小
   * @param where      条件
   * @param paras      参数
   * @return 分页对象
   */
  public Page<M> paginateBy(int pageNumber, int pageSize, String where, Object... paras) {
    return paginate(pageNumber, pageSize, getDialect().select(getTableMeta().getTableName(), getAlias(), where), paras);
  }

  /**
   * 分页查询
   *
   * @param pageNumber 页码
   * @param pageSize   每页大小
   * @param columns    列  用逗号分割
   * @param where      条件
   * @param paras      参数
   * @return 分页对象
   */
  public Page<M> paginateColsBy(int pageNumber, int pageSize, String columns, String where, Object... paras) {
    return paginate(pageNumber, pageSize, getDialect().select(getTableMeta().getTableName(), getAlias(), where, columns.split(",")), paras);
  }

  /**
   * 更新全部传入的列  UPDATE table SET name=?,age=? 参数 "abc",20
   *
   * @param columns 通过逗号分隔的列名 "name,age"
   * @param paras   按列名顺序排列参数   "abc",20
   * @return boolean
   */
  public boolean updateColsAll(String columns, Object... paras) {
    logger.warn("You must ensure that \"updateAll()\" method of safety.");
    return update(getDialect().update(getTableMeta().getTableName(), columns.split(",")), paras);
  }

  /**
   * 根据条件和传入的列更新  UPDATE table SET name=?,age=? WHERE x=?  参数 "abc",20,12
   *
   * @param columns 通过逗号分隔的列   "name,age"
   * @param where   条件 x=?
   * @param paras   按列名顺序排列参数   "abc",20,12
   * @return boolean
   */
  public boolean updateColsBy(String columns, String where, Object... paras) {
    return update(getDialect().update(getTableMeta().getTableName(), getAlias(), where, columns.split(",")), paras);
  }

  /**
   * 删除全部数据
   *
   * @return boolean
   */
  public boolean deleteAll() {
    logger.warn("You must ensure that \"deleteAll()\" method of safety.");
    return update(getDialect().delete(getTableMeta().getTableName()));
  }

  /**
   * 根据条件删除数据
   *
   * @param where 条件
   * @param paras 参数
   * @return
   */
  public boolean deleteBy(String where, Object... paras) {
    return update(getDialect().delete(getTableMeta().getTableName(), where), paras);
  }

  /**
   * COUNT 函数求和
   *
   * @return Long
   */
  public Long countAll() {
    return DS.useDS(getTableMeta().getDsName()).queryFirst(getDialect().count(getTableMeta().getTableName()));
  }

  /**
   * COUNT 根据条件函数求和
   *
   * @return Long
   */
  public Long countBy(String where, Object... paras) {
    return DS.useDS(getTableMeta().getDsName()).queryFirst(getDialect().count(getTableMeta().getTableName(), getAlias(), where), paras);
  }


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
}
