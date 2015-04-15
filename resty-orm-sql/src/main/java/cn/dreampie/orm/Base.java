package cn.dreampie.orm;

import cn.dreampie.common.Constant;
import cn.dreampie.common.entity.Entity;
import cn.dreampie.common.entity.exception.EntityException;
import cn.dreampie.common.util.Joiner;
import cn.dreampie.log.Logger;
import cn.dreampie.orm.cache.QueryCache;
import cn.dreampie.orm.dialect.Dialect;
import cn.dreampie.orm.exception.DBException;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static cn.dreampie.common.util.Checker.checkArgument;
import static cn.dreampie.common.util.Checker.checkNotNull;

/**
 * Created by wangrenhui on 15/3/31.
 */
public abstract class Base<M extends Base> extends Entity<M> {

  private static final Logger logger = Logger.getLogger(Model.class);
  private static final boolean devMode = Constant.devMode;
  public static final String DEFAULT_PRIMARY_KAY = "id";

  /**
   * 获取当前实例数据表的元数据
   *
   * @return TableMeta
   */
  protected abstract TableMeta getTableMeta();


  /**
   * 获取数据源元数据
   *
   * @return DataSourceMeta
   */
  protected abstract DataSourceMeta getDataSourceMeta();

  /**
   * 获取数据库方言
   *
   * @return Dialect
   */
  protected Dialect getDialect() {
    return getDataSourceMeta().getDialect();
  }

  /**
   * 是否使用cache
   *
   * @return boolean
   */
  protected abstract boolean isUseCache();

  /**
   * 本次不实用缓存
   *
   * @return Model
   */
  public abstract M unCache();

  /**
   * 切换数据源
   *
   * @param useDS 数据源名称
   * @return Model
   */
  public abstract M useDS(String useDS);

  /**
   * 获取表的别名
   *
   * @return Alias
   */
  protected abstract String getAlias();

  /**
   * 设置表的别名
   *
   * @param alias 别名
   * @return model
   */
  protected abstract M setAlias(String alias);

  /**
   * 从缓存中读取数据
   *
   * @param sql    sql语句
   * @param params sql参数
   * @param <T>    返回的数据类型
   * @return T
   */
  protected <T> T getCache(String sql, Object[] params) {
    TableMeta tableMeta = getTableMeta();
    if (tableMeta.isCached()) {
      return (T) QueryCache.instance().get(getClass().getSimpleName(), tableMeta.getDsName(), tableMeta.getTableName(), sql, params);
    }
    return null;
  }

  /**
   * 添加到缓存
   *
   * @param sql    sql语句
   * @param params sql参数
   * @param cache  要缓存的数据
   */
  protected void addCache(String sql, Object[] params, Object cache) {
    TableMeta tableMeta = getTableMeta();
    if (tableMeta.isCached()) {
      QueryCache.instance().add(getClass().getSimpleName(), tableMeta.getDsName(), tableMeta.getTableName(), sql, params, cache);
    }
  }

  /**
   * 清除缓存 通过数据源名称＋表名称
   */
  protected void purgeCache() {
    TableMeta tableMeta = getTableMeta();
    if (tableMeta.isCached()) {
      QueryCache.instance().purge(getClass().getSimpleName(), tableMeta.getDsName(), tableMeta.getTableName());
    }
  }

  public boolean hasAttr(String attr) {
    return getTableMeta().hasAttr(attr);
  }

  /**
   * Check the table name. The table name must in sql.
   */
  protected void checkTableName(String tableName, String sql) {
    if (!sql.toLowerCase().contains(tableName.toLowerCase()))
      throw new DBException("The table name: " + tableName + " not in your sql.");
  }

  private void logSql(String sql, Object[][] params) {
    if (getDataSourceMeta().isShowSql() && logger.isInfoEnabled()) {
      StringBuilder log = new StringBuilder("Sql: {").append(sql).append("} ");
      if (params != null && params.length > 0) {
        for (Object[] para : params) {
          log.append(", params: ").append('{');
          log.append(Joiner.on("}, {").join(para));
          log.append('}');
        }
      }
      log.append('\n');
      logger.info(log.toString());
    }
  }

  private void logSql(String sql, Object[] params) {
    if (getDataSourceMeta().isShowSql() && logger.isInfoEnabled()) {
      StringBuilder log = new StringBuilder("Sql: {").append(sql).append("} ");
      if (params != null && params.length > 0) {
        log.append(", params: ").append('{');
        log.append(Joiner.on("}, {").join(params));
        log.append('}');
      }
      logger.info(log.toString());
    }
  }

  private void logSql(List<String> sqls) {
    if (getDataSourceMeta().isShowSql() && logger.isInfoEnabled()) {
      logger.info("Sqls: " + '{' + Joiner.on("}, {").join(sqls) + '}');
    }
  }

  private PreparedStatement getPreparedStatement(Connection conn, String primaryKey, String sql, Object[] params) throws SQLException {
    //打印sql语句
    logSql(sql, params);

    PreparedStatement pst = conn.prepareStatement(sql, new String[]{primaryKey == null ? DEFAULT_PRIMARY_KAY : primaryKey});

    for (int i = 0; i < params.length; i++) {
      pst.setObject(i + 1, params[i]);
    }
    return pst;
  }


  private PreparedStatement getPreparedStatement(Connection conn, String primaryKey, String sql, Object[][] params) throws SQLException {
    //打印sql语句
    logSql(sql, params);

    PreparedStatement pst = null;
    String key = primaryKey == null ? DEFAULT_PRIMARY_KAY : primaryKey;
    String[] returnKeys = new String[params.length];
    for (int i = 0; i < params.length; i++) {
      returnKeys[i] = key;
    }
    pst = conn.prepareStatement(sql, returnKeys);
    final int batchSize = 1000;
    int count = 0;
    for (Object[] para : params) {
      for (int j = 0; j < para.length; j++) {
        pst.setObject(j + 1, para[j]);
      }
      pst.addBatch();
      if (++count % batchSize == 0) {
        pst.executeBatch();
      }
    }
    return pst;
  }

  private Statement getPreparedStatement(Connection conn, List<String> sqls) throws SQLException {
    //打印sql语句
    logSql(sqls);
    Statement stmt = null;

    stmt = conn.createStatement();
    final int batchSize = 1000;
    int count = 0;
    for (String aSql : sqls) {
      stmt.addBatch(aSql);
      if (++count % batchSize == 0) {
        stmt.executeBatch();
      }
    }
    return stmt;
  }

  /**
   * Get id after save method.
   */
  protected void getGeneratedKey(PreparedStatement pst, String pKey) throws SQLException {
    if (get(pKey) == null) {
      ResultSet rs = pst.getGeneratedKeys();
      if (rs.next()) {
        set(pKey, rs.getObject(1));    // It returns Long object for int colType
        rs.close();
      }
    }
  }

  /**
   * 获取主键
   */
  protected void getGeneratedKey(PreparedStatement pst, String pKey, List<? extends Entity> models) throws SQLException {
    ResultSet rs = pst.getGeneratedKeys();
    for (Entity<?> model : models) {
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
   * @param sql    an SQL statement that may contain one or more '?' IN parameter placeholders
   * @param params the parameters of sql
   * @return the list of Model
   */
  public List<M> find(String sql, Object... params) {
    List<M> result = null;
    boolean cached = false;
    boolean useCache = isUseCache();

    TableMeta tableMeta = getTableMeta();
    if (useCache) {
      cached = tableMeta.isCached();
      //hit cache
      if (cached) {
        result = getCache(sql, params);
      }
      if (result != null) {
        return result;
      }
    } else {
      logger.debug("This query not use cache.");
    }

    if (devMode)
      checkTableName(tableMeta.getTableName(), sql);

    DataSourceMeta dsm = getDataSourceMeta();
    Connection conn = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    try {
      conn = dsm.getConnection();
      pst = getPreparedStatement(conn, tableMeta.getPrimaryKey(), sql, params);
      rs = pst.executeQuery();
      result = BaseBuilder.build(rs, getClass(), dsm, tableMeta);
    } catch (SQLException e) {
      throw new DBException(e.getMessage(), e);
    } catch (InstantiationException e) {
      throw new EntityException(e.getMessage(), e);
    } catch (IllegalAccessException e) {
      throw new EntityException(e.getMessage(), e);
    } finally {
      dsm.close(rs, pst, conn);
    }
    //add cache
    if (cached) {
      addCache(sql, params, result);
    }
    return result;
  }

  /**
   * Find first model. I recommend add "limit 1" in your sql.
   *
   * @param sql    an SQL statement that may contain one or more '?' IN parameter placeholders
   * @param params the parameters of sql
   * @return Model
   */
  public M findFirst(String sql, Object... params) {
    List<M> result = find(sql, params);
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
   * @param params     参数
   * @return
   */
  public Page<M> paginate(int pageNumber, int pageSize, String sql, Object... params) {
    checkArgument(pageNumber >= 1 && pageSize >= 1, "pageNumber and pageSize must be more than 0");


    DataSourceMeta dsm = getDataSourceMeta();
    Dialect dialect = dsm.getDialect();

    long totalRow = 0;
    int totalPage = 0;

    List result = query(dialect.countWith(sql), params);
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
    List<M> list = find(dialect.paginateWith(pageNumber, pageSize, sql), params);
    return new Page<M>(list, pageNumber, pageSize, totalPage, (int) totalRow);
  }

  /**
   * Save model.
   *
   * @return boolean
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
      pst = getPreparedStatement(conn, tableMeta.getPrimaryKey(), sql, getModifyAttrValues());

      result = pst.executeUpdate();
      getGeneratedKey(pst, tableMeta.getPrimaryKey());
      clearModifyAttrs();
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
   * @return boolean
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
    Object[][] params = new Object[models.size()][columns.length];

    for (int i = 0; i < params.length; i++) {
      for (int j = 0; j < params[i].length; j++) {
        params[i][j] = models.get(i).get(columns[j]);
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

      pst = getPreparedStatement(conn, tableMeta.getPrimaryKey(), sql, params);
      result = pst.executeBatch();
      getGeneratedKey(pst, tableMeta.getPrimaryKey(), models);
      //没有事务的情况下 手动提交
      if (dsm.getCurrentConnection() == null)
        conn.commit();
      conn.setAutoCommit(autoCommit);
      for (M model : models) {
        model.clearModifyAttrs();
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

  /**
   * update sql
   *
   * @param sql    sql
   * @param params 参数
   * @return boolean
   */
  public boolean update(String sql, Object... params) {
    TableMeta tableMeta = getTableMeta();
    DataSourceMeta dsm = getDataSourceMeta();
    //清除缓存
    if (tableMeta.isCached()) {
      purgeCache();
    }
    if (devMode)
      checkTableName(tableMeta.getTableName(), sql);

    int result = -1;
    Connection conn = null;
    PreparedStatement pst = null;
    try {
      conn = dsm.getConnection();
      pst = getPreparedStatement(conn, DEFAULT_PRIMARY_KAY, sql, params);
      result = pst.executeUpdate();
    } catch (SQLException e) {
      throw new DBException(e.getMessage(), e);
    } finally {
      dsm.close(pst, conn);
    }
    return result > 0;
  }

  /**
   * Execute sql update
   */
  public boolean execute(String... sqls) {
    return execute(Arrays.asList(sqls));
  }

  /**
   * Execute a batch of SQL INSERT, UPDATE, or DELETE queries.
   * int[] result = DbPro.use().batch("myConfig", sqlList, 500);
   *
   * @param sqls The SQL list to execute.
   * @return The number of rows updated per statement
   */
  public boolean execute(List<String> sqls) {

    Statement stmt = null;
    int[] result = null;
    Connection conn = null;
    Boolean autoCommit = null;
    DataSourceMeta dsm = getDataSourceMeta();
    try {
      conn = dsm.getConnection();
      autoCommit = conn.getAutoCommit();
      if (autoCommit)
        conn.setAutoCommit(false);

      stmt = getPreparedStatement(conn, sqls);
      result = stmt.executeBatch();
      //没有事务的情况下 手动提交
      if (dsm.getCurrentConnection() == null)
        conn.commit();
      conn.setAutoCommit(autoCommit);

      for (int r : result) {
        if (r < 1) {
          return false;
        }
      }
      return true;
    } catch (SQLException e) {
      throw new DBException(e.getMessage(), e);
    } finally {
      dsm.close(stmt, conn);
    }
  }

  /**
   * 调用存储过程
   * int CallableStatement.executeUpdate: 存储过程不返回结果集。
   * ResultSet CallableStatement.executeQuery: 存储过程返回一个结果集。
   * Boolean CallableStatement.execute: 存储过程返回多个结果集。
   * int[] CallableStatement.executeBatch: 提交批处理命令到数据库执行。
   *
   * @param sql    存储过程的sql
   * @param inCall 执行请求  返回结果
   * @param <T>    返回类型
   * @return T
   */
  public <T> T call(String sql, InCall inCall) {
    Connection conn = null;
    CallableStatement cstmt = null;
    DataSourceMeta dsm = getDataSourceMeta();
    try {
      conn = dsm.getConnection();
      cstmt = conn.prepareCall(sql);
      return (T) inCall.call(cstmt);
    } catch (SQLException e) {
      throw new DBException(e.getMessage(), e);
    } finally {
      dsm.close(cstmt, conn);
    }
  }

  /**
   * Delete model.
   */
  public boolean delete() {
    TableMeta tableMeta = getTableMeta();
    Map<String, Object> attrs = getAttrs();

    Object id = attrs.get(tableMeta.getPrimaryKey());
    checkNotNull(id, "You can't delete model without primaryKey " + tableMeta.getPrimaryKey() + ".");

    //锁定主键 删除的时候 使用所有主键作为条件
    if (tableMeta.isLockKey()) {
      String[] primaryKeys = tableMeta.getPrimaryKeys();
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

    Map<String, Object> attrs = getAttrs();

    if (getModifyAttrs().isEmpty())
      return false;

    TableMeta tableMeta = getTableMeta();
    Dialect dialect = getDialect();

    String pKey = tableMeta.getPrimaryKey();
    Object id = attrs.get(pKey);
    checkNotNull(id, "You can't update model without Primary Key " + pKey + ".");

    String where = null;
    Object[] params = null;
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
      params = new Object[ids.length + modifys.length];
      System.arraycopy(modifys, 0, params, 0, modifys.length);
      System.arraycopy(ids, 0, params, modifys.length, ids.length);
      where = Joiner.on("=?,").join(tableMeta.getPrimaryKeys());
    } else {
      params = new Object[1 + modifys.length];
      System.arraycopy(modifys, 0, params, 0, modifys.length);
      params[modifys.length] = id;
      where = pKey;
    }
    String[] modifyNames = getModifyAttrNames();
    String sql = dialect.update(tableMeta.getTableName(), "", where + "=?", modifyNames);

    if (modifyNames.length <= 0) {  // Needn't update
      return false;
    }

    boolean result = update(sql, params);
    if (result) {
      clearModifyAttrs();
      return true;
    }
    return false;
  }

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
   * @param where  条件
   * @param params 参数
   * @return list
   */
  public List<M> findBy(String where, Object... params) {
    return find(getDialect().select(getTableMeta().getTableName(), getAlias(), where), params);
  }

  /**
   * 根据where条件查询model集合
   *
   * @param colums 列 用逗号分割
   * @param where  条件
   * @param params 参数
   * @return model集合
   */
  public List<M> findColsBy(String colums, String where, Object... params) {
    return find(getDialect().select(getTableMeta().getTableName(), getAlias(), where, colums.split(",")), params);
  }

  /**
   * 根据条件查询 前几位
   *
   * @param topNumber 前几位
   * @param where     条件
   * @param params    参数
   * @return list
   */
  public List<M> findTopBy(int topNumber, String where, Object... params) {
    return paginate(1, topNumber, getDialect().select(getTableMeta().getTableName(), getAlias(), where), params).getList();
  }

  /**
   * 根据条件查询 前几位
   *
   * @param topNumber 前几位
   * @param columns   列 用逗号分割
   * @param where     条件
   * @param params    参数
   * @return list
   */
  public List<M> findColsTopBy(int topNumber, String columns, String where, Object... params) {
    return paginate(1, topNumber, getDialect().select(getTableMeta().getTableName(), getAlias(), where, columns.split(",")), params).getList();
  }

  /**
   * 根据条件查询第一个对象
   *
   * @param where  条件
   * @param params 参数
   * @return model对象
   */
  public M findFirstBy(String where, Object... params) {
    return findFirst(getDialect().select(getTableMeta().getTableName(), getAlias(), where), params);
  }

  /**
   * 根据条件查询第一个对象
   *
   * @param columns 列 用逗号分割
   * @param where   条件
   * @param params  参数
   * @return model对象
   */
  public M findColsFirstBy(String columns, String where, Object... params) {
    return findFirst(getDialect().select(getTableMeta().getTableName(), getAlias(), where, columns.split(",")), params);
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
   * @param params     参数
   * @return 分页对象
   */
  public Page<M> paginateBy(int pageNumber, int pageSize, String where, Object... params) {
    return paginate(pageNumber, pageSize, getDialect().select(getTableMeta().getTableName(), getAlias(), where), params);
  }

  /**
   * 分页查询
   *
   * @param pageNumber 页码
   * @param pageSize   每页大小
   * @param columns    列  用逗号分割
   * @param where      条件
   * @param params     参数
   * @return 分页对象
   */
  public Page<M> paginateColsBy(int pageNumber, int pageSize, String columns, String where, Object... params) {
    return paginate(pageNumber, pageSize, getDialect().select(getTableMeta().getTableName(), getAlias(), where, columns.split(",")), params);
  }

  /**
   * 更新全部传入的列  UPDATE table SET name=?,age=? 参数 "abc",20
   *
   * @param columns 通过逗号分隔的列名 "name,age"
   * @param params  按列名顺序排列参数   "abc",20
   * @return boolean
   */
  public boolean updateColsAll(String columns, Object... params) {
    logger.warn("You must ensure that \"updateAll()\" method of safety.");
    return update(getDialect().update(getTableMeta().getTableName(), columns.split(",")), params);
  }

  /**
   * 根据条件和传入的列更新  UPDATE table SET name=?,age=? WHERE x=?  参数 "abc",20,12
   *
   * @param columns 通过逗号分隔的列   "name,age"
   * @param where   条件 x=?
   * @param params  按列名顺序排列参数   "abc",20,12
   * @return boolean
   */
  public boolean updateColsBy(String columns, String where, Object... params) {
    return update(getDialect().update(getTableMeta().getTableName(), getAlias(), where, columns.split(",")), params);
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
   * @param where  条件
   * @param params 参数
   * @return
   */
  public boolean deleteBy(String where, Object... params) {
    return update(getDialect().delete(getTableMeta().getTableName(), where), params);
  }

  /**
   * COUNT 函数求和
   *
   * @return Long
   */
  public Long countAll() {
    return queryFirst(getDialect().count(getTableMeta().getTableName()));
  }

  /**
   * COUNT 根据条件函数求和
   *
   * @return Long
   */
  public Long countBy(String where, Object... params) {
    return queryFirst(getDialect().count(getTableMeta().getTableName(), getAlias(), where), params);
  }

  /**
   * 返回不确定的数据类型
   *
   * @param sql    sql语句
   * @param params sql参数
   * @param <T>    返回的数据类型
   * @return List<T>
   */
  public <T> List<T> query(String sql, Object... params) {

    boolean cached = false;
    boolean useCache = isUseCache();
    TableMeta tableMeta = getTableMeta();

    List<T> result = null;
    if (useCache) {
      cached = tableMeta.isCached();
      //hit cache
      if (cached) {
        result = getCache(sql, params);
        if (result != null) {
          return result;
        }
      }
    } else {
      logger.debug("This query not use cache.");
    }

    DataSourceMeta dsm = getDataSourceMeta();
    Connection conn = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    result = new ArrayList<T>();
    try {
      conn = dsm.getConnection();
      pst = getPreparedStatement(conn, DEFAULT_PRIMARY_KAY, sql, params);
      rs = pst.executeQuery();
      int colAmount = rs.getMetaData().getColumnCount();
      if (colAmount > 1) {
        while (rs.next()) {
          Object[] temp = new Object[colAmount];
          for (int i = 0; i < colAmount; i++) {
            temp[i] = rs.getObject(i + 1);
          }
          result.add((T) temp);
        }
      } else if (colAmount == 1) {
        while (rs.next()) {
          result.add((T) rs.getObject(1));
        }
      }
    } catch (SQLException e) {
      throw new DBException(e.getMessage(), e);
    } finally {
      dsm.close(rs, pst, conn);
    }
    //add cache
    if (cached) {
      addCache(sql, params, result);
    }
    return result;
  }

  /**
   * Execute sql query and return the first result. I recommend add "limit 1" in your sql.
   *
   * @param sql    an SQL statement that may contain one or more '?' IN parameter placeholders
   * @param params the parameters of sql
   * @return Object[] if your sql has select more than one column,
   * and it return Object if your sql has select only one column.
   */
  public <T> T queryFirst(String sql, Object... params) {
    List<T> result = query(sql, params);
    return result.size() > 0 ? result.get(0) : null;
  }

  /**
   * Execute sql query just return one column.
   *
   * @param <T>    the type of the column that in your sql's select statement
   * @param sql    an SQL statement that may contain one or more '?' IN parameter placeholders
   * @param params the parameters of sql
   * @return List<T>
   */
  public <T> T queryColumn(String sql, Object... params) {
    List<T> result = query(sql, params);
    if (result.size() > 0) {
      T temp = result.get(0);
      if (temp instanceof Object[])
        throw new DBException("Only one column can be queried.");
      return temp;
    }
    return null;
  }

  public String queryStr(String sql, Object... params) {
    return (String) queryColumn(sql, params);
  }

  public Integer queryInt(String sql, Object... params) {
    return (Integer) queryColumn(sql, params);
  }

  public Long queryLong(String sql, Object... params) {
    return (Long) queryColumn(sql, params);
  }

  public Double queryDouble(String sql, Object... params) {
    return (Double) queryColumn(sql, params);
  }

  public Float queryFloat(String sql, Object... params) {
    return (Float) queryColumn(sql, params);
  }

  public BigDecimal queryBigDecimal(String sql, Object... params) {
    return (BigDecimal) queryColumn(sql, params);
  }

  public byte[] queryBytes(String sql, Object... params) {
    return (byte[]) queryColumn(sql, params);
  }

  public Date queryDate(String sql, Object... params) {
    return (Date) queryColumn(sql, params);
  }

  public Time queryTime(String sql, Object... params) {
    return (Time) queryColumn(sql, params);
  }

  public Timestamp queryTimestamp(String sql, Object... params) {
    return (Timestamp) queryColumn(sql, params);
  }

  public Boolean queryBoolean(String sql, Object... params) {
    return (Boolean) queryColumn(sql, params);
  }

  public Number queryNumber(String sql, Object... params) {
    return (Number) queryColumn(sql, params);
  }
}
