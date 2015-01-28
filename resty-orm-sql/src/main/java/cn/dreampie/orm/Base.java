package cn.dreampie.orm;

import cn.dreampie.common.Constant;
import cn.dreampie.common.Entity;
import cn.dreampie.common.util.Joiner;
import cn.dreampie.common.util.json.Jsoner;
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
  /**
   * Attributes of this model
   */
  private Map<String, Object> attrs = getAttrsMap();

  protected <T> T getCache(String sql, Object[] paras) {
    ModelMeta modelMeta = getModelMeta();
    if (modelMeta.isCached()) {
      return (T) QueryCache.instance().get(modelMeta.getDsName(), modelMeta.getTableName(), sql, paras);
    }
    return null;
  }

  protected void addCache(String sql, Object[] paras, Object cache) {
    ModelMeta modelMeta = getModelMeta();
    if (modelMeta.isCached()) {
      QueryCache.instance().add(modelMeta.getDsName(), modelMeta.getTableName(), sql, paras, cache);
    }
  }

  protected void purgeCache() {
    ModelMeta modelMeta = getModelMeta();
    if (modelMeta.isCached()) {
      QueryCache.instance().purge(modelMeta.getDsName(), modelMeta.getTableName());
    }
  }

  /**
   * Return columns map.
   */
  public Map<String, Object> getAttrsMap() {
    if (attrs == null) {
      attrs = new CaseInsensitiveMap<Object>();
    }
    return attrs;
  }

  /**
   * Flag of column has been modified. update need this flag
   */
  private Map<String, Object> modifyFlag;

  public Map<String, Object> getModifyFlag() {
    if (modifyFlag == null) {
      modifyFlag = new CaseInsensitiveMap<Object>();
    }
    return modifyFlag;
  }


  /**
   * Return attribute name of this model.
   */
  public String[] getModifyNames() {
    Set<String> attrNameSet = modifyFlag.keySet();
    return attrNameSet.toArray(new String[attrNameSet.size()]);
  }

  /**
   * Return attribute values of this model.
   */
  public Object[] getModifyValues() {
    java.util.Collection<Object> attrValueCollection = modifyFlag.values();
    return attrValueCollection.toArray(new Object[attrValueCollection.size()]);
  }

  protected ModelMeta getModelMeta() {
    return Metadatas.getModelMeta(getClass());
  }

  protected DataSourceMeta getDataSourceMeta() {
    return Metadatas.getDataSourceMeta(getModelMeta().getDsName());
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
    if (getModelMeta().hasAttribute(attr)) {
      attrs.put(attr, value);
      getModifyFlag().put(attr, value);  // Add modify flag, update() need this flag.
      return (M) this;
    }
    throw new DBException("The attribute name is not exists: " + attr);
  }

  /**
   * Put key value pair to the model when the key is not attribute of the model.
   */
  public M put(String key, Object value) {
    if (getModelMeta().hasAttribute(key))
      getModifyFlag().put(key, value);
    attrs.put(key, value);
    return (M) this;
  }

  /**
   * Get attribute of any mysql type
   */
  public <T> T get(String attr) {
    return (T) (attrs.get(attr));
  }

  /**
   * Get attribute of any mysql type. Returns defaultValue if null.
   */
  public <T> T get(String attr, Object defaultValue) {
    Object result = attrs.get(attr);
    return (T) (result != null ? result : defaultValue);
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
   * Return attribute Set.
   */
  public Set<Map.Entry<String, Object>> getAttrsEntrySet() {
    return attrs.entrySet();
  }

  /**
   * Get attribute of mysql type: varchar, char, enum, set, text, tinytext, mediumtext, longtext
   */
  public String getStr(String attr) {
    return (String) attrs.get(attr);
  }

  /**
   * Get attribute of mysql type: int, integer, tinyint(n) n > 1, smallint, mediumint
   */
  public Integer getInt(String attr) {
    return (Integer) attrs.get(attr);
  }

  /**
   * Get attribute of mysql type: bigint, unsign int
   */
  public Long getLong(String attr) {
    return (Long) attrs.get(attr);
  }

  /**
   * Get attribute of mysql type: unsigned bigint
   */
  public java.math.BigInteger getBigInteger(String attr) {
    return (java.math.BigInteger) attrs.get(attr);
  }

  /**
   * Get attribute of mysql type: date, year
   */
  public java.util.Date getDate(String attr) {
    return (java.util.Date) attrs.get(attr);
  }

  /**
   * Get attribute of mysql type: time
   */
  public java.sql.Time getTime(String attr) {
    return (java.sql.Time) attrs.get(attr);
  }

  /**
   * Get attribute of mysql type: timestamp, datetime
   */
  public java.sql.Timestamp getTimestamp(String attr) {
    return (java.sql.Timestamp) attrs.get(attr);
  }

  /**
   * Get attribute of mysql type: real, double
   */
  public Double getDouble(String attr) {
    return (Double) attrs.get(attr);
  }

  /**
   * Get attribute of mysql type: float
   */
  public Float getFloat(String attr) {
    return (Float) attrs.get(attr);
  }

  /**
   * Get attribute of mysql type: bit, tinyint(1)
   */
  public Boolean getBoolean(String attr) {
    return (Boolean) attrs.get(attr);
  }

  /**
   * Get attribute of mysql type: decimal, numeric
   */
  public java.math.BigDecimal getBigDecimal(String attr) {
    return (java.math.BigDecimal) attrs.get(attr);
  }

  /**
   * Get attribute of mysql type: binary, varbinary, tinyblob, blob, mediumblob, longblob
   */
  public byte[] getBytes(String attr) {
    return (byte[]) attrs.get(attr);
  }

  /**
   * Get attribute of any type that extends from Number
   */
  public Number getNumber(String attr) {
    return (Number) attrs.get(attr);
  }

  private PreparedStatement getPreparedStatement(Connection conn, String sql, Object[] paras) throws SQLException {
    PreparedStatement pst = null;
    pst = conn.prepareStatement(sql, new String[]{getModelMeta().getPrimaryKey()});

    for (int i = 0; i < paras.length; i++) {
      pst.setObject(i + 1, paras[i]);
    }
    return pst;
  }

  private PreparedStatement getPreparedStatement(Connection connection, String sql, Object[][] paras) throws SQLException {
    PreparedStatement pst = null;
    String key = getModelMeta().getPrimaryKey();
    String[] returnKeys = new String[paras.length];
    for (int i = 0; i < paras.length; i++) {
      returnKeys[i] = key;
    }
    pst = connection.prepareStatement(sql, returnKeys);
    final int batchSize = 1000;
    int count = 0;
    for (int i = 0; i < paras.length; i++) {
      for (int j = 0; j < paras[i].length; j++) {
        pst.setObject(j + 1, paras[i][j]);
      }
      pst.addBatch();
      if (++count % batchSize == 0) {
        pst.executeBatch();
      }
    }
    return pst;
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
    boolean cached = getModelMeta().isCached();
    //hit cache
    if (cached) {
      result = getCache(sql, paras);
    }
    if (result != null) {
      return result;
    }

    if (Constant.dev_mode)
      checkTableName(getModelMeta(), sql);

    DataSourceMeta dsm = getDataSourceMeta();
    Connection conn = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    try {
      conn = dsm.getConnection();
      pst = getPreparedStatement(conn, sql, paras);
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
    return findById("*", id);
  }

  public M findByIds(Object... ids) {
    return findByIds("*", ids);
  }

  /**
   * Find model by id. Fetch the specific columns only.
   * Example: User user = User.dao.findById(15, "name, age");
   *
   * @param columns the specific columns
   * @param id      the id value of the model
   */
  public M findById(String columns, Object id) {
    String sql = getDialect().select(getModelMeta().getTableName(), "", getModelMeta().getPrimaryKey() + "=?", columns.split(","));
    List<M> result = find(sql, id);
    return result.size() > 0 ? result.get(0) : null;
  }

  public M findByIds(String columns, Object... ids) {
    String sql = getDialect().select(getModelMeta().getTableName(), "", Joiner.on("=? AND ").join(getModelMeta().getPrimaryKeys()) + "=?", columns.split(","));
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
    if (getModelMeta().isCached()) {
      purgeCache();
    }

    DataSourceMeta dsm = getDataSourceMeta();
    Dialect dialect = dsm.getDialect();
    ModelMeta modelMeta = getModelMeta();

    String sql = dialect.insert(modelMeta.getTableName(), getAttrNames());

    // --------
    Connection conn = null;
    PreparedStatement pst = null;
    int result = 0;
    try {
      conn = dsm.getConnection();
      pst = getPreparedStatement(conn, sql, getAttrValues());

      result = pst.executeUpdate();
      getGeneratedKey(pst, modelMeta);
      getModifyFlag().clear();
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
    //清除models缓存
    if (firstModel.getModelMeta().isCached()) {
      firstModel.purgeCache();
    }

    DataSourceMeta dsm = firstModel.getDataSourceMeta();
    Dialect dialect = dsm.getDialect();
    ModelMeta modelMeta = firstModel.getModelMeta();

    String[] columns = firstModel.getAttrNames();
    String sql = dialect.insert(modelMeta.getTableName(), columns);

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

      pst = getPreparedStatement(conn, sql, paras);
      result = pst.executeBatch();
      getGeneratedKey(pst, modelMeta, models);
      //没有事务的情况下 手动提交
      if (dsm.getCurrentConnection() == null)
        conn.commit();
      conn.setAutoCommit(autoCommit);
      for (M model : models) {
        model.getModifyFlag().clear();
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


  /**
   * Get id after save method.
   */
  private void getGeneratedKey(PreparedStatement pst, ModelMeta modelMeta) throws SQLException {
    String pKey = modelMeta.getPrimaryKey();

    if (get(pKey) == null) {
      ResultSet rs = pst.getGeneratedKeys();
      if (rs.next()) {
        set(pKey, rs.getObject(1));    // It returns Long object for int colType
        rs.close();
      }
    }
  }

  private void getGeneratedKey(PreparedStatement pst, ModelMeta modelMeta, List<M> models) throws SQLException {
    ResultSet rs = pst.getGeneratedKeys();
    String pKey = null;
    for (M model : models) {
      pKey = model.getModelMeta().getPrimaryKey();
      if (model.get(pKey) == null) {
        if (rs.next()) {
          model.set(pKey, rs.getObject(1));
        }
      }
    }
    rs.close();
  }

  //update  base
  protected int update(String sql, Object... paras) {
    //清除缓存
    if (getModelMeta().isCached()) {
      purgeCache();
    }
    if (Constant.dev_mode)
      checkTableName(getModelMeta(), sql);
    return DS.use(getModelMeta().getDsName()).update(sql, paras);
  }

  /**
   * Delete model.
   */
  public boolean delete() {
    ModelMeta modelMeta = getModelMeta();


    Object id = attrs.get(modelMeta.getPrimaryKey());
    checkNotNull(id, "You can't delete model without primaryKey " + modelMeta.getPrimaryKey() + ".");

    //锁定主键 删除的时候 使用所有主键作为条件
    if (modelMeta.isLockKey()) {
      String[] pkeys = modelMeta.getPrimaryKeys();
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
    return deleteById(getModelMeta(), id);
  }

  public boolean deleteByIds(Object... ids) {
    checkNotNull(ids, "You can't delete model without primaryKey.");
    return deleteByIds(getModelMeta(), ids);
  }

  private boolean deleteById(ModelMeta modelMeta, Object id) {
    String sql = getDialect().delete(modelMeta.getTableName(), modelMeta.getPrimaryKey() + "=?");
    int result = update(sql, id);
    return result > 0;
  }

  private boolean deleteByIds(ModelMeta modelMeta, Object... ids) {

    String sql = getDialect().delete(modelMeta.getTableName(), Joiner.on("=? AND ").join(modelMeta.getPrimaryKeys()) + "=?");
    int result = update(sql, ids);
    return result > 0;
  }

  /**
   * Update model.
   */
  public boolean update() {
    if (getModifyFlag().isEmpty())
      return false;

    ModelMeta modelMeta = getModelMeta();
    Dialect dialect = getDialect();

    String pKey = modelMeta.getPrimaryKey();
    Object id = attrs.get(pKey);
    checkNotNull(id, "You can't update model without Primary Key " + pKey + ".");

    String where = null;
    Object[] paras = null;
    Object[] modifys = getModifyValues();
    //锁定主键 更新的时候 使用所有主键作为条件
    if (modelMeta.isLockKey()) {
      String[] pkeys = modelMeta.getPrimaryKeys();
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
      where = Joiner.on("=?,").join(modelMeta.getPrimaryKeys());
    } else {
      paras = new Object[1 + modifys.length];
      System.arraycopy(modifys, 0, paras, 0, modifys.length);
      paras[modifys.length] = id;
      where = pKey;
    }

    String sql = dialect.update(modelMeta.getTableName(), "", where + "=?", getModifyNames());

    if (getModifyNames().length <= 0) {  // Needn't update
      return false;
    }

    int result = update(sql, paras);
    if (result >= 1) {
      getModifyFlag().clear();
      return true;
    }
    return false;
  }

  /**
   * Check the table name. The table name must in sql.
   */
  private void checkTableName(ModelMeta modelMeta, String sql) {
    if (!sql.toLowerCase().contains(modelMeta.getTableName().toLowerCase()))
      throw new DBException("The table name: " + modelMeta.getTableName() + " not in your sql.");
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

  public M putAttrs(M model) {
    return (M) putAttrs(model.getAttrs());
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
    getModifyFlag().remove(attr);
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
        this.getModifyFlag().remove(a);
      }
    return (M) this;
  }

  /**
   * Remove attributes if it is null.
   *
   * @return this model
   */
  public M removeNullValueAttrs() {
    for (Iterator<Map.Entry<String, Object>> it = attrs.entrySet().iterator(); it.hasNext(); ) {
      Map.Entry<String, Object> e = it.next();
      if (e.getValue() == null) {
        it.remove();
        getModifyFlag().remove(e.getKey());
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
        if (this.getModifyFlag().containsKey(a))
          newModifyFlag.put(a, this.attrs.get(a));
      }
      this.attrs = newAttrs;
      this.modifyFlag = newModifyFlag;
    } else {
      this.attrs.clear();
      this.getModifyFlag().clear();
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
      boolean keepFlag = getModifyFlag().containsKey(attr);
      attrs.clear();
      getModifyFlag().clear();
      attrs.put(attr, keepIt);
      if (keepFlag)
        getModifyFlag().put(attr, keepIt);
    } else {
      attrs.clear();
      getModifyFlag().clear();
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
    getModifyFlag().clear();
    return (M) this;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(super.toString()).append(" {");
    boolean first = true;
    for (Map.Entry<String, Object> e : attrs.entrySet()) {
      if (first)
        first = false;
      else
        sb.append(", ");

      Object value = e.getValue();
      if (value != null)
        value = value.toString();
      sb.append(e.getKey()).append(":").append(value);
    }
    sb.append("}");
    return sb.toString();
  }

  public boolean equals(Object o) {
    if (!(o instanceof Base))
      return false;
    if (o == this)
      return true;
    return this.attrs.equals(((Base) o).attrs);
  }

  public int hashCode() {
    return (attrs == null ? 0 : attrs.hashCode()) ^ (getModifyFlag() == null ? 0 : getModifyFlag().hashCode());
  }

  /**
   * Return attribute name of this model.
   */
  public String[] getAttrNames() {
    Set<String> attrNameSet = attrs.keySet();
    return attrNameSet.toArray(new String[attrNameSet.size()]);
  }

  /**
   * Return attribute values of this model.
   */
  public Object[] getAttrValues() {
    java.util.Collection<Object> attrValueCollection = attrs.values();
    return attrValueCollection.toArray(new Object[attrValueCollection.size()]);
  }

  /**
   * Return json string of this model.
   */
  public String toJson() {
    return Jsoner.toJSONString(attrs);
  }

}
