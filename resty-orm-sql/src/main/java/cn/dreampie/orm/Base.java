package cn.dreampie.orm;

import cn.dreampie.common.Constant;
import cn.dreampie.common.Entity;
import cn.dreampie.common.util.json.Jsoner;
import cn.dreampie.orm.cache.QueryCache;
import cn.dreampie.orm.dialect.Dialect;
import cn.dreampie.orm.exception.DBException;
import cn.dreampie.orm.exception.ModelException;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static cn.dreampie.common.util.Checker.checkArgument;
import static cn.dreampie.common.util.Checker.checkNotNull;

/**
 * Created by ice on 14-12-30.
 */
public abstract class Base<M extends Base> extends Entity<Base> implements Serializable {
  /**
   * Attributes of this model
   */
  private Map<String, Object> attrs = getAttrsMap();

  protected <T> T getCache(String sql, Object[] paras) {
    ModelMeta modelMeta = getModelMeta();
    if (modelMeta.cached()) {
      return (T) QueryCache.instance().getItem(modelMeta.getDsName(), modelMeta.getTableName(), sql, paras);
    }
    return null;
  }

  protected void addCache(String sql, Object[] paras, Object cache) {
    ModelMeta modelMeta = getModelMeta();
    if (modelMeta.cached()) {
      QueryCache.instance().addItem(modelMeta.getDsName(), modelMeta.getTableName(), sql, paras, cache);
    }
  }

  protected void purgeCache() {
    ModelMeta modelMeta = getModelMeta();
    if (modelMeta.cached()) {
      QueryCache.instance().purgeTableCache(modelMeta.getDsName(), modelMeta.getTableName());
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

  private PreparedStatement getPreparedStatement(String sql, Object[] paras) throws SQLException {
    return getPreparedStatement(getDataSourceMeta(), sql, paras);
  }

  private PreparedStatement getPreparedStatement(String dsName, String sql, Object[] paras) throws SQLException {
    if (dsName == null) return getPreparedStatement(sql, paras);
    return getPreparedStatement(Metadatas.getDataSourceMeta(dsName), sql, paras);
  }

  private PreparedStatement getPreparedStatement(DataSourceMeta dsm, String sql, Object[] paras) throws SQLException {
    PreparedStatement pst = null;
    if (dsm.getDialect().getDbType().equalsIgnoreCase("oracle")) {
      pst = dsm.getConnection().prepareStatement(sql, new String[]{getModelMeta().getPrimaryKey()});
    } else {
      pst = dsm.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    }
    for (int i = 0; i < paras.length; i++) {
      pst.setObject(i + 1, paras[i]);
    }
    return pst;
  }

  public Long count(String sql, Object... paras) {
    return DS.use(getModelMeta().getDsName()).queryFirst(sql, paras);
  }

  public Long count(String sql) {
    return DS.use(getModelMeta().getDsName()).queryFirst(sql);
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
    boolean cached = getModelMeta().cached();
    if (cached) {
      result = getCache(sql, paras);
    }
    if (result != null) {
      return result;
    }

    Class<? extends Base> modelClass = getClass();
    if (Constant.dev_mode)
      checkTableName(modelClass, sql);
    try {
      PreparedStatement pst = getPreparedStatement(sql, paras);
      ResultSet rs = pst.executeQuery();
      result = ModelBuilder.build(rs, modelClass);
      getDataSourceMeta().close(rs, pst);
    } catch (SQLException e) {
      throw new DBException(e);
    } catch (InstantiationException e) {
      throw new ModelException(e);
    } catch (IllegalAccessException e) {
      throw new ModelException(e);
    }
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
    return findById(id, "*");
  }

  /**
   * Find model by id. Fetch the specific columns only.
   * Example: User user = User.dao.findById(15, "name, age");
   *
   * @param id      the id value of the model
   * @param columns the specific columns
   */
  public M findById(Object id, String columns) {
    String sql = getDialect().select(getModelMeta().getTableName(), getModelMeta().getPrimaryKey() + "=?", columns.split(","));
    List<M> result = find(sql, id);
    return result.size() > 0 ? result.get(0) : null;
  }

  /**
   * @param pageNo   页码
   * @param pageSize 每页数量
   * @param sql      sql语句
   * @param paras    参数
   * @return
   */
  public Page<M> paginate(int pageNo, int pageSize, String sql, Object... paras) {
    checkArgument(pageNo >= 1 && pageSize >= 1, "pageNo and pageSize must be more than 0");

    DataSourceMeta dsm = getDataSourceMeta();
    Dialect dialect = dsm.getDialect();

    long totalRow = 0;
    int totalPage = 0;
    List result = find(dialect.countWith(sql), paras);
    int size = result.size();
    if (size == 1)
      totalRow = ((Number) result.get(0)).longValue();    // totalRow = (Long)result.get(0);
    else if (size > 1)
      totalRow = result.size();
    else
      return new Page<M>(new ArrayList<M>(0), pageNo, pageSize, 0, 0);  // totalRow = 0;

    totalPage = (int) (totalRow / pageSize);
    if (totalRow % pageSize != 0) {
      totalPage++;
    }

    // --------
    List<M> list = find(dialect.paginateWith(pageNo, pageSize, sql), paras);
    return new Page<M>(list, pageNo, pageSize, totalPage, (int) totalRow);
  }

  /**
   * @see #paginate(int, int, String, Object...)
   */
  public Page<M> paginate(int pageNo, int pageSize, String sql) {
    return paginate(pageNo, pageSize, sql, DS.NULL_PARA_ARRAY);
  }

  /**
   * Save model.
   */
  public boolean save() {
    //清除缓存
    if (getModelMeta().cached()) {
      purgeCache();
    }


    DataSourceMeta dsm = getDataSourceMeta();
    Dialect dialect = dsm.getDialect();
    ModelMeta modelMeta = getModelMeta();

    String sql = dialect.insert(modelMeta.getTableName(), getAttrNames());

    // --------
    PreparedStatement pst = null;
    int result = 0;
    try {
      pst = getPreparedStatement(sql, getAttrValues());

      result = pst.executeUpdate();
      getGeneratedKey(pst, modelMeta);
      getModifyFlag().clear();
      return result >= 1;
    } catch (SQLException e) {
      throw new DBException(e);
    } finally {
      dsm.close(pst);
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


  /**
   * Delete model.
   */
  public boolean delete() {
    ModelMeta modelMeta = getModelMeta();
    Object id = attrs.get(modelMeta.getPrimaryKey());
    if (id == null)
      throw new ModelException("You can't delete model without primaryKey.");
    return deleteById(id);
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

  private boolean deleteById(ModelMeta modelMeta, Object id) {
    //清除缓存
    if (getModelMeta().cached()) {
      purgeCache();
    }
    String sql = getDialect().delete(modelMeta.getTableName(), modelMeta.getPrimaryKey() + "=?");
    int result = update(sql, id);
    return result > 0;
  }

  //update  base
  protected int update(String sql, Object... paras) {
    //清除缓存
    if (getModelMeta().cached()) {
      purgeCache();
    }
    return DS.use(getModelMeta().getDsName()).update(sql, paras);
  }

  public int update(String columns) {
    ModelMeta modelMeta = getModelMeta();
    Dialect dialect = getDialect();
    return update(dialect.update(modelMeta.getTableName(), columns.split(",")), DS.NULL_PARA_ARRAY);
  }

  public int update(String columns, String where, Object... paras) {
    ModelMeta modelMeta = getModelMeta();
    Dialect dialect = getDialect();
    return update(dialect.update(modelMeta.getTableName(), where, columns.split(",")), paras);
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
    checkNotNull(id, "You can't update model without Primary Key.");

    String sql = dialect.update(modelMeta.getTableName(), pKey + "=?", getModifyNames());

    if (getModifyNames().length <= 0) {  // Needn't update
      return false;
    }

    int result = update(sql, getModifyValues());
    if (result >= 1) {
      getModifyFlag().clear();
      return true;
    }
    return false;
  }


  /**
   * Check the table name. The table name must in sql.
   */
  private void checkTableName(Class<? extends Base> modelClass, String sql) {
    ModelMeta modelMeta = getModelMeta();
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
