package cn.dreampie.orm;

import cn.dreampie.common.Constant;
import cn.dreampie.common.entity.CaseInsensitiveMap;
import cn.dreampie.common.entity.Entity;
import cn.dreampie.common.util.Joiner;
import cn.dreampie.log.Logger;
import cn.dreampie.orm.cache.QueryCache;
import cn.dreampie.orm.dialect.Dialect;
import cn.dreampie.orm.exception.DBException;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static cn.dreampie.common.util.Checker.checkArgument;
import static cn.dreampie.common.util.Checker.checkNotNull;

/**
 * Record
 */
public class Record extends Entity<Record> implements Serializable {
  private static final Logger logger = Logger.getLogger(Record.class);
  private static final boolean devMode = Constant.devMode;

  private DataSourceMeta dataSourceMeta;
  private TableMeta tableMeta;

  private Map<String, Object> attrs = new CaseInsensitiveMap<Object>();

  private Record() {
  }

  public static Record use(String tableName) {
    return Record.use(tableName, false);
  }

  public static Record use(String tableName, boolean cached) {
    return Record.use(tableName, DS.DEFAULT_PRIMARY_KAY);
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

  public static Record useDS(String dsName, String tableName) {
    return Record.useDS(dsName, tableName, DS.DEFAULT_PRIMARY_KAY);
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

  public static Record useDS(DataSourceMeta dataSourceMeta, String tableName, String pKeys, boolean lockKey, boolean cached) {
    checkNotNull(dataSourceMeta, "Could not found dataSourceMeta.");
    checkNotNull(tableName, "Could not found tableName.");
    Record record = new Record();
    record.dataSourceMeta = dataSourceMeta;
    String dsName = dataSourceMeta.getDsName();
    if (Metadata.hasRecordTableMeta(dsName, tableName)) {
      record.tableMeta = Metadata.getRecordTableMeta(dsName, tableName);
    } else {
      record.tableMeta = TableMetaBuilder.buildRecord(new TableMeta(dsName, tableName, pKeys, lockKey, cached), dataSourceMeta);
    }
    return record;
  }


  /**
   * create new record
   *
   * @return Record
   */
  public Record reNew() {
    Record record = new Record();
    record.dataSourceMeta = dataSourceMeta;
    record.tableMeta = tableMeta;
    return record;
  }

  /**
   * Return attrs map.
   */
  public Map<String, Object> getAttrs() {
    return attrs;
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

  /**
   * Set attribute to model.
   *
   * @param attr  the attribute name of the model
   * @param value the value of the attribute
   * @return this model
   * @throws cn.dreampie.orm.exception.DBException if the attribute is not exists of the model
   */
  public Record set(String attr, Object value) {
    if (tableMeta.hasAttribute(attr)) {
      attrs.put(attr, value);
      modifyAttrs.put(attr, value);  // Add modify flag, update() need this flag.
      return this;
    }
    throw new DBException("The attribute name is not exists: " + attr);
  }

  /**
   * Put key value pair to the model when the key is not attribute of the model.
   */
  public Record put(String key, Object value) {
    if (tableMeta.hasAttribute(key))
      modifyAttrs.put(key, value);
    attrs.put(key, value);
    return this;
  }


  /**
   * Check the table name. The table name must in sql.
   */
  private void checkTableName(String tableName, String sql) {
    if (!sql.toLowerCase().contains(tableName.toLowerCase()))
      throw new DBException("The table name: " + tableName + " not in your sql.");
  }


  /**
   * Get id after insert method getGeneratedKey().
   */
  private void getGeneratedKey(PreparedStatement pst, String primaryKey) throws SQLException {
    ResultSet rs = pst.getGeneratedKeys();
    if (rs.next())
      set(primaryKey, rs.getObject(1));
    rs.close();
  }

  private void getGeneratedKey(PreparedStatement pst, String primaryKey, List<Record> records) throws SQLException {
    ResultSet rs = pst.getGeneratedKeys();
    for (Record record : records) {
      if (record.get(primaryKey) == null) {
        if (rs.next()) {
          record.set(primaryKey, rs.getObject(1));
        }
      }
    }
    rs.close();
  }

  public List<Record> find(String sql, Object... paras) {
    List<Record> result = null;
    String dsName = dataSourceMeta.getDsName();
    boolean cached = tableMeta.isCached();
    //hit cache
    if (cached) {
      result = QueryCache.instance().get(dsName, sql, paras);
    }
    if (result != null) {
      return result;
    }

    Connection conn = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    try {
      conn = dataSourceMeta.getConnection();
      pst = DS.getPreparedStatement(conn, DS.DEFAULT_PRIMARY_KAY, sql, paras);
      rs = pst.executeQuery();
      result = RecordBuilder.build(rs, dataSourceMeta, tableMeta);
    } catch (SQLException e) {
      throw new DBException(e.getMessage(), e);
    } finally {
      dataSourceMeta.close(rs, pst, conn);
    }
    //添加缓存
    if (cached) {
      QueryCache.instance().add(dataSourceMeta.getDsName(), sql, paras, result);
    }
    return result;
  }


  /**
   * @param sql the sql statement
   * @see #find(String, Object...)
   */
  public List<Record> find(String sql) {
    return find(sql, DS.NULL_PARA_ARRAY);
  }

  /**
   * Find first record. I recommend add "limit 1" in your sql.
   *
   * @param sql   an SQL statement that may contain one or more '?' IN parameter placeholders
   * @param paras the parameters of sql
   * @return the Record object
   */
  public Record findFirst(String sql, Object... paras) {
    List<Record> result = find(sql, paras);
    return result.size() > 0 ? result.get(0) : null;
  }

  /**
   * @param sql an SQL statement
   * @see #findFirst(String, Object...)
   */
  public Record findFirst(String sql) {
    List<Record> result = find(sql, DS.NULL_PARA_ARRAY);
    return result.size() > 0 ? result.get(0) : null;
  }

  /**
   * Find record by id.
   * Example: Record user = DbPro.use().findById("user", 15);
   *
   * @param id the id value of the record
   */
  public Record findById(Object id) {
    return findColsById("*", id);
  }

  public Record findByIds(Object... ids) {
    return findColsByIds("*", ids);
  }

  /**
   * Find record by id. Fetch the specific columns only.
   * Example: Record user = DbPro.use().findById("user", "user_id", 15, "name, age");
   *
   * @param id      the id value of the record
   * @param columns the specific columns separate with comma character ==> ","
   */
  public Record findColsById(String columns, Object id) {
    checkNotNull(id, "You can't find model without Primary Key.");

    String sql = dataSourceMeta.getDialect().select(tableMeta.getTableName(), "", tableMeta.getPrimaryKey() + "=?", columns.split(","));
    List<Record> result = find(sql, id);
    return result.size() > 0 ? result.get(0) : null;
  }

  /**
   * Find record by ids. Fetch the specific columns only.
   * Example: Record user = DbPro.use().findById("user", "user_id", 15, "name, age");
   *
   * @param ids     the id values of the record
   * @param columns the specific columns separate with comma character ==> ","
   */
  public Record findColsByIds(String columns, Object[] ids) {
    checkNotNull(ids, "You can't find model without Primary Keys.");

    String sql = dataSourceMeta.getDialect().select(tableMeta.getTableName(), "", Joiner.on("=? AND ").join(tableMeta.getPrimaryKeys()) + "=?", columns.split(","));
    List<Record> result = find(sql, ids);
    return result.size() > 0 ? result.get(0) : null;
  }

  /**
   * 分页查询Record
   *
   * @param pageNumber 页码
   * @param pageSize   页大小
   * @param sql        sql
   * @param paras      参数
   * @return page
   */
  public Page<Record> paginate(int pageNumber, int pageSize, String sql, Object... paras) {
    checkArgument(pageNumber >= 1 || pageSize >= 1, "pageNumber and pageSize must be more than 0");

    Dialect dialect = dataSourceMeta.getDialect();

    long totalRow = 0;
    int totalPage = 0;
    List result = DS.use(dataSourceMeta.getDsName()).query(dialect.countWith(sql), paras);
    int size = result.size();
    if (size == 1)
      totalRow = ((Number) result.get(0)).longValue();
    else if (size > 1)
      totalRow = result.size();
    else
      return new Page<Record>(new ArrayList<Record>(0), pageNumber, pageSize, 0, 0);

    totalPage = (int) (totalRow / pageSize);
    if (totalRow % pageSize != 0) {
      totalPage++;
    }

    // --------
    List<Record> list = find(dialect.paginateWith(pageNumber, pageSize, sql), paras);
    return new Page<Record>(list, pageNumber, pageSize, totalPage, (int) totalRow);
  }


  /**
   * @see #paginate(int, int, String, Object...)
   */
  public Page<Record> paginate(int pageNumber, int pageSize, String sql) {
    return paginate(pageNumber, pageSize, sql, DS.NULL_PARA_ARRAY);
  }

  public boolean save() {
    String sql = dataSourceMeta.getDialect().insert(tableMeta.getTableName(), getModifyAttrNames());
    int result = -1;

    boolean cached = tableMeta.isCached();
    String primaryKey = tableMeta.getPrimaryKey();
    //remove cache
    if (cached) {
      QueryCache.instance().purge(dataSourceMeta.getDsName(), tableMeta.getTableName());
    }
    Connection conn = null;
    PreparedStatement pst = null;
    try {
      conn = dataSourceMeta.getConnection();
      pst = DS.getPreparedStatement(conn, primaryKey, sql, getModifyAttrValues());
      result = pst.executeUpdate();
      getGeneratedKey(pst, primaryKey);
      modifyAttrs.clear();
    } catch (SQLException e) {
      throw new DBException(e.getMessage(), e);
    } finally {
      dataSourceMeta.close(pst, conn);
    }
    return result >= 1;
  }

  public boolean save(Record... records) {
    return save(Arrays.asList(records));
  }

  /**
   * 批量保存record
   *
   * @param records 记录
   * @return boolean
   */
  public boolean save(List<Record> records) {
    if (records == null || records.size() <= 0) {
      logger.warn("Cloud not found records to save.");
      return false;
    }

    Record firstRecord = records.get(0);

    if (records.size() == 1) {
      return firstRecord.save();
    }

    boolean cached = tableMeta.isCached();
    String primaryKey = tableMeta.getPrimaryKey();
    //清除models缓存
    if (cached) {
      QueryCache.instance().purge(dataSourceMeta.getDsName(), tableMeta.getTableName());
    }

    String[] columns = firstRecord.getModifyAttrNames();
    String sql = dataSourceMeta.getDialect().insert(tableMeta.getTableName(), columns);
    //参数
    Object[][] paras = new Object[records.size()][columns.length];

    for (int i = 0; i < paras.length; i++) {
      for (int j = 0; j < paras[i].length; j++) {
        paras[i][j] = records.get(i).get(columns[j]);
      }
    }

    // --------
    PreparedStatement pst = null;
    int[] result = null;
    Connection conn = null;
    Boolean autoCommit = null;
    try {
      conn = dataSourceMeta.getConnection();
      autoCommit = conn.getAutoCommit();
      if (autoCommit)
        conn.setAutoCommit(false);

      pst = DS.getPreparedStatement(conn, primaryKey, sql, paras);
      result = pst.executeBatch();
      getGeneratedKey(pst, primaryKey, records);
      //没有事务的情况下 手动提交
      if (dataSourceMeta.getCurrentConnection() == null)
        conn.commit();
      conn.setAutoCommit(autoCommit);

      for (Record record : records) {
        record.getModifyAttrs().clear();
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
      dataSourceMeta.close(pst, conn);
    }
  }

  //update  base
  public int update(String sql, Object... paras) {

    boolean cached = tableMeta.isCached();
    //清除缓存
    if (cached) {
      QueryCache.instance().purge(dataSourceMeta.getDsName(), tableMeta.getTableName());
    }
    if (devMode)
      checkTableName(tableMeta.getTableName(), sql);
    return DS.use(dataSourceMeta.getDsName()).update(sql, paras);
  }


  /**
   * Delete record.
   */
  public boolean delete() {

    boolean lockKey = tableMeta.isLockKey();
    String primaryKey = tableMeta.getPrimaryKey();
    String[] primaryKeys = tableMeta.getPrimaryKeys();
    Object id = attrs.get(primaryKey);
    checkNotNull(id, "You can't delete model without primaryKey " + primaryKey + ".");

    //锁定主键 删除的时候 使用所有主键作为条件
    if (lockKey) {
      Object[] ids = new Object[primaryKeys.length];
      ids[0] = id;
      int i = 1;
      for (String idKey : primaryKeys) {
        ids[i] = attrs.get(idKey);
        i++;
      }
      return deleteByIds(ids);
    } else {
      return deleteById(id);
    }
  }

  /**
   * Delete record by id.
   *
   * @param id the id value of the record
   * @return true if delete succeed otherwise false
   */
  public boolean deleteById(Object id) {
    checkNotNull(id, "You can't delete model without Primary Key.");

    String sql = dataSourceMeta.getDialect().delete(tableMeta.getTableName(), tableMeta.getPrimaryKey() + "=?");
    int result = update(sql, id);
    return result > 0;
  }

  /**
   * Delete record by ids.
   * Example: boolean succeed = DbPro.use().deleteById("user", "user_id", 15);
   *
   * @param ids the id values of the record
   * @return true if delete succeed otherwise false
   */
  public boolean deleteByIds(Object... ids) {
    checkNotNull(ids, "You can't delete model without Primary Keys.");

    String sql = dataSourceMeta.getDialect().delete(tableMeta.getTableName(), Joiner.on("=? AND ").join(tableMeta.getPrimaryKeys()) + "=?");
    int result = update(sql, ids);
    return result > 0;
  }


  public boolean update() {

    boolean lockKey = tableMeta.isLockKey();
    String primaryKey = tableMeta.getPrimaryKey();
    String[] primaryKeys = tableMeta.getPrimaryKeys();
    Object id = get(primaryKey);

    checkNotNull(id, "You can't update model without Primary Key " + primaryKey + ".");

    String where = null;
    Object[] paras = null;
    Object[] modifys = getModifyAttrValues();
    //锁定主键 更新的时候 使用所有主键作为条件
    if (lockKey) {
      Object[] ids = new Object[primaryKeys.length];
      ids[0] = id;
      int i = 1;
      for (String idKey : primaryKeys) {
        ids[i] = attrs.get(idKey);
        i++;
      }
      paras = new Object[ids.length + modifys.length];
      System.arraycopy(modifys, 0, paras, 0, modifys.length);
      System.arraycopy(ids, 0, paras, modifys.length, ids.length);
      where = Joiner.on("=?,").join(primaryKeys);
    } else {
      paras = new Object[1 + modifys.length];
      System.arraycopy(modifys, 0, paras, 0, modifys.length);
      paras[modifys.length] = id;
      where = primaryKey;
    }
    String[] modifyNames = getModifyAttrNames();
    String sql = dataSourceMeta.getDialect().update(tableMeta.getTableName(), "", where + "=?", modifyNames);

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
   * @param record the Model
   * @return this Model
   */
  public Record setAttrs(Record record) {
    return setAttrs(record.getAttrs());
  }

  /**
   * Set attributes with Map.
   *
   * @param attrs attributes of this model
   * @return this Model
   */
  public Record setAttrs(Map<String, Object> attrs) {
    for (Map.Entry<String, Object> e : attrs.entrySet())
      set(e.getKey(), e.getValue());
    return this;
  }


  public Record putAttrs(Map<String, Object> attrs) {
    for (Map.Entry<String, Object> e : attrs.entrySet())
      put(e.getKey(), e.getValue());
    return this;
  }

  /**
   * Remove attribute of this model.
   *
   * @param attr the attribute name of the model
   * @return this model
   */
  public Record remove(String attr) {
    attrs.remove(attr);
    modifyAttrs.remove(attr);
    return this;
  }

  /**
   * Remove attributes of this model.
   *
   * @param attrs the attribute name of the model
   * @return this model
   */
  public Record remove(String... attrs) {
    if (attrs != null)
      for (String a : attrs) {
        this.attrs.remove(a);
        this.modifyAttrs.remove(a);
      }
    return this;
  }

  /**
   * Remove attributes if it is null.
   *
   * @return this model
   */
  public Record removeNull() {
    for (Iterator<Map.Entry<String, Object>> it = attrs.entrySet().iterator(); it.hasNext(); ) {
      Map.Entry<String, Object> e = it.next();
      if (e.getValue() == null) {
        it.remove();
        modifyAttrs.remove(e.getKey());
      }
    }
    return this;
  }

  /**
   * Keep attributes of this model and remove other attributes.
   *
   * @param attrs the attribute name of the model
   * @return this model
   */
  public Record keep(String... attrs) {
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
    return this;
  }

  /**
   * Keep attribute of this model and remove other attributes.
   *
   * @param attr the attribute name of the model
   * @return this model
   */
  public Record keep(String attr) {
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
    return this;
  }

  /**
   * Remove all attributes of this model.
   *
   * @return this model
   */
  public Record clear() {
    attrs.clear();
    modifyAttrs.clear();
    return this;
  }

  public int hashCode() {
    return (attrs == null ? 0 : attrs.hashCode()) ^ (modifyAttrs == null ? 0 : modifyAttrs.hashCode());
  }


  //////////////From Model////////////////////

  protected String alias;


  /**
   * 查询全部的model数据
   *
   * @return model 集合
   */
  public List<Record> findAll() {
    return find(dataSourceMeta.getDialect().select(tableMeta.getTableName()));
  }

  /**
   * 查询全部的model数据
   *
   * @param columns 列 用逗号分割
   * @return model 集合
   */
  public List<Record> findColsAll(String columns) {
    return find(dataSourceMeta.getDialect().select(tableMeta.getTableName(), columns.split(",")));
  }

  /**
   * 根据where条件查询model集合
   *
   * @param where 条件
   * @param paras 参数
   * @return list
   */
  public List<Record> findBy(String where, Object... paras) {
    return find(dataSourceMeta.getDialect().select(tableMeta.getTableName(), getAlias(), where), paras);
  }

  /**
   * 根据where条件查询model集合
   *
   * @param colums 列 用逗号分割
   * @param where  条件
   * @param paras  参数
   * @return model集合
   */
  public List<Record> findColsBy(String colums, String where, Object... paras) {
    return find(dataSourceMeta.getDialect().select(tableMeta.getTableName(), getAlias(), where, colums.split(",")), paras);
  }

  /**
   * 根据条件查询 前几位
   *
   * @param topNumber 前几位
   * @param where     条件
   * @param paras     参数
   * @return list
   */
  public List<Record> findTopBy(int topNumber, String where, Object... paras) {
    return paginate(1, topNumber, dataSourceMeta.getDialect().select(tableMeta.getTableName(), getAlias(), where), paras).getList();
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
  public List<Record> findColsTopBy(int topNumber, String columns, String where, Object... paras) {
    return paginate(1, topNumber, dataSourceMeta.getDialect().select(tableMeta.getTableName(), getAlias(), where, columns.split(",")), paras).getList();
  }

  /**
   * 根据条件查询第一个对象
   *
   * @param where 条件
   * @param paras 参数
   * @return model对象
   */
  public Record findFirstBy(String where, Object... paras) {
    return findFirst(dataSourceMeta.getDialect().select(tableMeta.getTableName(), getAlias(), where), paras);
  }

  /**
   * 根据条件查询第一个对象
   *
   * @param columns 列 用逗号分割
   * @param where   条件
   * @param paras   参数
   * @return model对象
   */
  public Record findColsFirstBy(String columns, String where, Object... paras) {
    return findFirst(dataSourceMeta.getDialect().select(tableMeta.getTableName(), getAlias(), where, columns.split(",")), paras);
  }

  /**
   * 分页查询
   *
   * @param pageNumber 页码
   * @param pageSize   每页大小
   * @return 分页对象
   */
  public Page<Record> paginateAll(int pageNumber, int pageSize) {
    return paginate(pageNumber, pageSize, dataSourceMeta.getDialect().select(tableMeta.getTableName()));
  }

  /**
   * 分页查询
   *
   * @param pageNumber 页码
   * @param pageSize   每页大小
   * @param columns    列 用逗号分割
   * @return 分页对象
   */
  public Page<Record> paginateColsAll(int pageNumber, int pageSize, String columns) {
    return paginate(pageNumber, pageSize, dataSourceMeta.getDialect().select(tableMeta.getTableName(), columns.split(",")));
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
  public Page<Record> paginateBy(int pageNumber, int pageSize, String where, Object... paras) {
    return paginate(pageNumber, pageSize, dataSourceMeta.getDialect().select(tableMeta.getTableName(), getAlias(), where), paras);
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
  public Page<Record> paginateColsBy(int pageNumber, int pageSize, String columns, String where, Object... paras) {
    return paginate(pageNumber, pageSize, dataSourceMeta.getDialect().select(tableMeta.getTableName(), getAlias(), where, columns.split(",")), paras);
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
    return update(dataSourceMeta.getDialect().update(tableMeta.getTableName(), columns.split(",")), paras) > 0;
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
    return update(dataSourceMeta.getDialect().update(tableMeta.getTableName(), getAlias(), where, columns.split(",")), paras) > 0;
  }

  /**
   * 删除全部数据
   *
   * @return boolean
   */
  public boolean deleteAll() {
    logger.warn("You must ensure that \"deleteAll()\" method of safety.");
    return update(dataSourceMeta.getDialect().delete(tableMeta.getTableName())) > 0;
  }

  /**
   * 根据条件删除数据
   *
   * @param where 条件
   * @param paras 参数
   * @return
   */
  public boolean deleteBy(String where, Object... paras) {
    return update(dataSourceMeta.getDialect().delete(tableMeta.getTableName(), where), paras) > 0;
  }

  /**
   * COUNT 函数求和
   *
   * @return Long
   */
  public Long countAll() {
    return DS.use(dataSourceMeta.getDsName()).queryFirst(dataSourceMeta.getDialect().count(tableMeta.getTableName()));
  }

  /**
   * COUNT 根据条件函数求和
   *
   * @return Long
   */
  public Long countBy(String where, Object... paras) {
    return DS.use(dataSourceMeta.getDsName()).queryFirst(dataSourceMeta.getDialect().count(tableMeta.getTableName(), getAlias(), where), paras);
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
  public Record setAlias(String alias) {
    this.alias = alias;
    return this;
  }

}




