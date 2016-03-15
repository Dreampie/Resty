package cn.dreampie.orm;

import cn.dreampie.common.entity.Conversion;
import cn.dreampie.common.entity.Entity;
import cn.dreampie.common.entity.exception.EntityException;
import cn.dreampie.common.util.Joiner;
import cn.dreampie.log.Logger;
import cn.dreampie.orm.cache.QueryCache;
import cn.dreampie.orm.callable.ObjectCall;
import cn.dreampie.orm.callable.ResultSetCall;
import cn.dreampie.orm.dialect.Dialect;
import cn.dreampie.orm.exception.DBException;
import cn.dreampie.orm.generate.Generator;
import cn.dreampie.orm.page.FullPage;
import cn.dreampie.orm.page.Page;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
public abstract class Base<M extends Base> extends Entity<M> implements Externalizable {

  public static final String DEFAULT_GENERATED_KEY = "id";
  private final Logger logger = Logger.getLogger(getClass());
  private String alias;

  /**
   * 获取实际的数据操作对象
   *
   * @return class
   */
  protected Class<? extends M> getMClass() {
    Class clazz = getClass();
    Type[] actualTypeArguments = ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments();
    if (actualTypeArguments.length > 0) {
      return (Class<? extends M>) actualTypeArguments[0];
    } else {
      return (Class<? extends M>) clazz;
    }
  }

  public Conversion getConversion(String attr) {
    return null;
  }

  /**
   * 获取当前实例数据表的元数据
   *
   * @return TableMeta
   */
  protected abstract TableMeta getTableMeta();

  /**
   * 判断是否是表的属性
   *
   * @param attr 属性名
   * @return
   */
  public boolean hasColumn(String attr) {
    return getTableMeta().hasColumn(attr);
  }

  /**
   * 获取改数据库列对应的java类型
   *
   * @param attr 属性名
   * @return class
   */
  public Class getColumnType(String attr) {
    return getDialect().getColumnType(getTableMeta().getDataType(attr));
  }


  /**
   * 获取数据源元数据
   *
   * @return DataSourceMeta
   */
  protected DataSourceMeta getDataSourceMeta() {
    return Metadata.getDataSourceMeta(getTableMeta().getDsmName());
  }

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
   * 本次不使用缓存
   *
   * @return Model
   */
  public abstract M unCache();

  /**
   * 切换数据源
   *
   * @param dsmName 数据源名称
   * @return Model
   */
  public abstract M useDSM(String dsmName);

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
      throw new EntityException("Model alias only set once.");
    this.alias = alias;
    return (M) this;
  }

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
      return (T) QueryCache.instance().get(tableMeta.getDsmName(), tableMeta.getTableName(), getMClass().getSimpleName(), sql, params);
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
      QueryCache.instance().add(tableMeta.getDsmName(), tableMeta.getTableName(), getMClass().getSimpleName(), sql, params, cache, tableMeta.getExpired());
    }
  }

  /**
   * 清除缓存 通过数据源名称＋表名称
   */
  public void purgeCache() {
    TableMeta tableMeta = getTableMeta();
    if (tableMeta.isCached()) {
      QueryCache.instance().purge(tableMeta.getDsmName(), tableMeta.getTableName());
    }
  }

  /**
   * 删除指定sql＋params的缓存
   *
   * @param sql    sql语句
   * @param params sql参数
   */
  protected void removeCache(String sql, Object[] params) {
    TableMeta tableMeta = getTableMeta();
    if (tableMeta.isCached()) {
      QueryCache.instance().remove(tableMeta.getDsmName(), tableMeta.getTableName(), getMClass().getSimpleName(), sql, params);
    }
  }

  /**
   * sql语句
   *
   * @param sql    sql
   * @param params 参数
   */
  private void logSql(boolean showSql, String sql, Object[][] params) {
    if (showSql && logger.isInfoEnabled()) {
      StringBuilder log = new StringBuilder("Sql: {").append(sql).append("} ");
      if (params != null && params.length > 0) {
        int i = 0;
        for (Object[] para : params) {
          log.append(", params[").append(i++).append("]: ").append('{');
          log.append(Joiner.on("}, {").useForNull("null").join(para));
          log.append('}');
        }
      }
      log.append('\n');
      logger.info(log.toString());
    }
  }

  /**
   * sql 语句
   *
   * @param sql    sql
   * @param params 参数
   */
  private void logSql(boolean showSql, String sql, Object[] params) {
    if (showSql && logger.isInfoEnabled()) {
      StringBuilder log = new StringBuilder("Sql: {").append(sql).append("} ");
      if (params != null && params.length > 0) {
        log.append(", params: ").append('{');
        log.append(Joiner.on("}, {").useForNull("null").join(params));
        log.append('}');
      }
      logger.info(log.toString());
    }
  }

  /**
   * sql 语句
   *
   * @param sqls sqls
   */
  private void logSql(boolean showSql, List<String> sqls) {
    if (showSql && logger.isInfoEnabled()) {
      logger.info("Sqls: " + '{' + Joiner.on("}, {").useForNull("null").join(sqls) + '}');
    }
  }


  /**
   * sql连接对象
   *
   * @return Connection
   * @throws SQLException
   */
  private Connection getWriteConnection(DataSourceMeta dataSourceMeta) throws SQLException {
    dataSourceMeta.beginTransaction();
    return dataSourceMeta.getWriteConnection();
  }

  private Connection getReadConnection(DataSourceMeta dataSourceMeta) throws SQLException {
    dataSourceMeta.beginTransaction();
    return dataSourceMeta.getReadConnection();
  }

  /**
   * 获取sql执行对象
   *
   * @param conn
   * @param tableMeta
   * @param sql
   * @param params
   * @return
   * @throws SQLException
   */
  private PreparedStatement getPreparedStatement(boolean showSql, boolean needGeneratedKey, Connection conn, TableMeta tableMeta, String sql, Object[] params) throws SQLException {
    //打印sql语句
    logSql(showSql, sql, params);
    PreparedStatement pst;
    //如果没有自动生成的主键 则不获取
    String generatedKey = tableMeta.getGeneratedKey();
    boolean generated = tableMeta.getGenerator() == null && !generatedKey.isEmpty();
    if (generated && needGeneratedKey) {
      pst = conn.prepareStatement(sql, new String[]{generatedKey});
    } else {
      pst = conn.prepareStatement(sql);
    }
    for (int i = 0; i < params.length; i++) {
      pst.setObject(i + 1, params[i]);
    }
    return pst;
  }

  /**
   * 获取sql执行对象
   *
   * @param conn
   * @param tableMeta
   * @param sql
   * @param params
   * @return
   * @throws SQLException
   */
  private PreparedStatement getPreparedStatement(boolean showSql, boolean needGeneratedKey, Connection conn, TableMeta tableMeta, String sql, Object[][] params) throws SQLException {
    //打印sql语句
    logSql(showSql, sql, params);

    PreparedStatement pst = null;
    //如果没有自动生成的主键 则不获取
    String generatedKey = tableMeta.getGeneratedKey();
    boolean generated = tableMeta.getGenerator() == null && !generatedKey.isEmpty();
    if (generated && needGeneratedKey) {
      String[] returnKeys = new String[params.length];
      for (int i = 0; i < params.length; i++) {
        returnKeys[i] = generatedKey;
      }
      pst = conn.prepareStatement(sql, returnKeys);
    } else {
      pst = conn.prepareStatement(sql);
    }
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

  /**
   * 获取sql执行对象
   *
   * @param conn
   * @param sqls
   * @return
   * @throws SQLException
   */
  private Statement getPreparedStatement(boolean showSql, Connection conn, List<String> sqls) throws SQLException {
    //打印sql语句
    logSql(showSql, sqls);
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
   * 获取主键
   *
   * @param tableMeta
   * @return
   */
  private String getPrimaryKey(TableMeta tableMeta) {
    String generatedKey = tableMeta.getGeneratedKey();
    if (generatedKey.isEmpty()) {
      String[] primaryKeys = getPrimaryKeys(tableMeta);
      if (primaryKeys.length > 0) {
        generatedKey = primaryKeys[0];
      } else {
        throw new IllegalArgumentException("Your table must have least one generatedKey or primaryKey.");
      }
    }
    return generatedKey;
  }

  /**
   * 获取所有的主键key
   *
   * @return
   */
  private String[] getPrimaryKeys(TableMeta tableMeta) {
    String generatedKey = tableMeta.getGeneratedKey();
    String[] primaryKey = tableMeta.getPrimaryKey();
    String[] keys;
    int i = 0;
    if (!generatedKey.isEmpty()) {
      keys = new String[primaryKey.length + 1];
      keys[i++] = generatedKey;
    } else {
      keys = new String[primaryKey.length];
    }
    for (String pKey : primaryKey) {
      keys[i++] = pKey;
    }
    if (keys.length <= 0) {
      throw new IllegalArgumentException("Your table must have least one generatedKey or primaryKey.");
    }
    return keys;
  }

  /**
   * 获取所有的主键值
   *
   * @return
   */
  private Object[] getPrimaryValues(TableMeta tableMeta) {
    Map<String, Object> attrs = getAttrs();
    String generatedKey = tableMeta.getGeneratedKey();
    Object id = null;
    if (!generatedKey.isEmpty()) {
      id = attrs.get(generatedKey);
      checkNotNull(id, "You can't delete model without generatedKey " + generatedKey + ".");
    }

    String[] primaryKeys = tableMeta.getPrimaryKey();
    Object[] values;
    int i = 0;
    if (!generatedKey.isEmpty()) {
      values = new Object[primaryKeys.length + 1];
      values[i++] = id;
    } else {
      values = new Object[primaryKeys.length];
    }

    for (String pKey : primaryKeys) {
      values[i++] = attrs.get(pKey);
    }
    if (values.length <= 0) {
      throw new IllegalArgumentException("Your must set generatedKey or primaryKey.");
    }
    return values;
  }

  /**
   * Get id after save method.
   */
  protected void setGeneratedKey(PreparedStatement pst, TableMeta tableMeta) throws SQLException {
    String generatedKey = tableMeta.getGeneratedKey();
    boolean generated = tableMeta.getGenerator() == null && !generatedKey.isEmpty();
    if (generated) {
      if (get(generatedKey) == null) {
        ResultSet rs = pst.getGeneratedKeys();
        if (rs.next()) {
          set(generatedKey, rs.getObject(1));    // It returns Long object for int colType
          rs.close();
        }
      }
    }
  }

  /**
   * 获取主键
   */
  protected void setGeneratedKey(PreparedStatement pst, TableMeta tableMeta, List<? extends Entity> models) throws SQLException {
    String generatedKey = tableMeta.getGeneratedKey();
    boolean generated = tableMeta.getGenerator() == null && !generatedKey.isEmpty();
    if (generated) {
      ResultSet rs = pst.getGeneratedKeys();
      for (Entity<?> model : models) {
        if (model.get(generatedKey) == null) {
          if (rs.next()) {
            model.set(generatedKey, rs.getObject(1));
          }
        }
      }
      rs.close();
    }
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
    boolean useCache = isUseCache();

    TableMeta tableMeta = getTableMeta();
    if (useCache) {
      //hit cache
      result = getCache(sql, params);

      if (result != null) {
        return result;
      }
    } else {
      logger.debug("This query not use cache.");
    }

    DataSourceMeta dsm = getDataSourceMeta();
    boolean showSql = dsm.isReadShowSql();
    Connection conn = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    try {
      conn = getReadConnection(dsm);
      pst = getPreparedStatement(showSql, false, conn, tableMeta, sql, params);
      rs = pst.executeQuery();
      result = BaseBuilder.build(rs, getMClass(), dsm, tableMeta);
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
    addCache(sql, params, result);
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
    TableMeta tableMeta = getTableMeta();
    List<M> result = find(tableMeta.getDialect().paginateWith(1, 1, sql), params);
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

  /**
   * Find model by multi id
   *
   * @param ids
   * @return
   */
  public M findByIds(Object... ids) {
    return findColsByIds("*", ids);
  }


  /**
   * Find model by ids
   *
   * @param ids
   * @return
   */
  public List<M> findInIds(Object... ids) {
    return findColsInIds("*", ids);
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
    String key = getPrimaryKey(tableMeta);
    Dialect dialect = getDialect();
    String sql = dialect.select(tableMeta.getTableName(), "", key + "=?", columns.split(","));
    return findFirst(sql, id);
  }

  public M findColsByIds(String columns, Object... ids) {
    TableMeta tableMeta = getTableMeta();
    String[] keys = getPrimaryKeys(tableMeta);
    String sql = getDialect().select(tableMeta.getTableName(), "", Joiner.on("=? AND ").join(keys) + "=?", columns.split(","));
    return findFirst(sql, ids);
  }

  public List<M> findColsInIds(String columns, Object... ids) {
    TableMeta tableMeta = getTableMeta();
    String key = getPrimaryKey(tableMeta);
    Dialect dialect = getDialect();
    StringBuilder appendQuestions = new StringBuilder();
    for (int i = 0; i < ids.length; i++) {
      if (i == 0) {
        appendQuestions.append("?");
      } else {
        appendQuestions.append(",?");
      }
    }

    String sql = dialect.select(tableMeta.getTableName(), "", key + " IN (" + appendQuestions + ")", columns.split(","));
    return find(sql, ids);
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
    List<M> list = find(dialect.paginateWith(pageNumber, pageSize, sql), params);
    return new Page<M>(list, pageNumber, pageSize);
  }

  /**
   * @param pageNumber 页码
   * @param pageSize   每页数量
   * @param sql        sql语句
   * @param params     参数
   * @return
   */
  public FullPage<M> fullPaginate(int pageNumber, int pageSize, String sql, Object... params) {
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
      return new FullPage<M>(new ArrayList<M>(0), pageNumber, pageSize, 0, 0);  // totalRow = 0;

    totalPage = (int) (totalRow / pageSize);
    if (totalRow % pageSize != 0) {
      totalPage++;
    }

    // --------
    List<M> list = find(dialect.paginateWith(pageNumber, pageSize, sql), params);
    return new FullPage<M>(list, pageNumber, pageSize, totalPage, (int) totalRow);
  }

  /**
   * Save model.
   *
   * @return boolean
   */
  public boolean save() {
    TableMeta tableMeta = getTableMeta();
    //清除缓存
    purgeCache();

    String generatedKey = tableMeta.getGeneratedKey();
    Generator generator = tableMeta.getGenerator();

    boolean generated = generator == null && !generatedKey.isEmpty();
    if (!generated && generator != null && get(generatedKey) == null) {
      set(generatedKey, generator.generateKey());
    }

    DataSourceMeta dsm = getDataSourceMeta();
    boolean showSql = dsm.isWriteShowSql();
    Dialect dialect = dsm.getDialect();
    String[] columns;
    if (generated) {
      columns = getModifyAttrNames(generatedKey);
    } else {
      columns = getModifyAttrNames();
    }

    Object[] params;
    if (generated) {
      params = getModifyAttrValues(generatedKey);
    } else {
      params = getModifyAttrValues();
    }

    //判断是否有更新
    if (columns.length <= 0) {
      logger.warn("Could not found any data to save.");
      return false;
    } else {
      String sql = dialect.insert(tableMeta.getTableName(), tableMeta.getGeneratedKey(), tableMeta.getSequence(), columns);
      Connection conn = null;
      PreparedStatement pst = null;
      int result = 0;
      try {
        conn = getWriteConnection(dsm);
        pst = getPreparedStatement(showSql, true, conn, tableMeta, sql, params);

        result = pst.executeUpdate();
        setGeneratedKey(pst, tableMeta);
        clearModifyAttrs();
        return result >= 1;
      } catch (SQLException e) {
        throw new DBException(e.getMessage(), e);
      } finally {
        dsm.close(pst, conn);
      }
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
    firstModel.purgeCache();

    String generatedKey = tableMeta.getGeneratedKey();
    //是否需要主键生成器生成值
    Generator generator = tableMeta.getGenerator();

    boolean generated = generator == null && !generatedKey.isEmpty();
    if (!generated && generator != null && get(generatedKey) == null) {
      firstModel.set(generatedKey, generator.generateKey());
    }

    DataSourceMeta dsm = firstModel.getDataSourceMeta();
    boolean showSql = dsm.isWriteShowSql();
    Dialect dialect = dsm.getDialect();

    String[] columns;
    if (generated) {
      columns = firstModel.getModifyAttrNames(generatedKey);
    } else {
      columns = firstModel.getModifyAttrNames();
    }

    //判断是否有更新
    if (columns.length <= 0) {
      logger.warn("Could not found any data to save.");
      return false;
    } else {
      String sql = dialect.insert(tableMeta.getTableName(), columns);
      //参数
      Object[][] params = new Object[models.size()][columns.length];

      String name;
      Object value;
      Conversion conversion;

      for (int i = 0; i < params.length; i++) {
        for (int j = 0; j < params[i].length; j++) {
          //如果是自动生成主键 使用生成器生成
          if (!generated && columns[j].equals(generatedKey) && models.get(i).get(generatedKey) == null) {
            models.get(i).set(columns[j], generator.generateKey());
          }
          
          name = columns[j];
          value = models.get(i).get(name);
          conversion = models.get(i).getConversion(name);
          if (conversion != null) {
            value = conversion.write(value);
          }

          params[i][j] = value;
        }
      }

      Connection conn = null;
      PreparedStatement pst = null;
      Boolean autoCommit = null;
      int[] result = null;
      try {
        conn = getWriteConnection(dsm);
        autoCommit = conn.getAutoCommit();
        if (autoCommit) {
          conn.setAutoCommit(false);
        }
        pst = getPreparedStatement(showSql, true, conn, tableMeta, sql, params);
        result = pst.executeBatch();
        setGeneratedKey(pst, tableMeta, models);
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
    boolean showSql = dsm.isWriteShowSql();
    //清除缓存
    purgeCache();

    int result = -1;
    Connection conn = null;
    PreparedStatement pst = null;
    try {
      conn = getWriteConnection(dsm);
      pst = getPreparedStatement(showSql, true, conn, tableMeta, sql, params);
      result = pst.executeUpdate();
    } catch (SQLException e) {
      throw new DBException(e.getMessage(), e);
    } finally {
      dsm.close(pst, conn);
    }
    return result > 0;
  }


  /**
   * Update model.
   */
  public boolean update() {
    TableMeta tableMeta = getTableMeta();
    Dialect dialect = getDialect();

    String generatedKey = tableMeta.getGeneratedKey();

    String[] columns = getModifyAttrNames(generatedKey);
    if (columns.length <= 0) {
      logger.warn("Could not found any modified attributes.");
      return false;
    }

    boolean hasGeneratedKey = !generatedKey.isEmpty();
    //是否使用数据库自增
    Object id = null;
    if (hasGeneratedKey) {
      id = get(generatedKey);
      checkNotNull(id, "You can't update model without Generated Key " + generatedKey + ".");
    }
    String where = null;
    Object[] params = null;
    Object[] values = getModifyAttrValues(generatedKey);

    //锁定主键 更新的时候 使用所有主键作为条件
    Object[] ids;
    String[] keys;
    String[] pkeys = tableMeta.getPrimaryKey();
    int i = 0;
    int j = 0;
    if (hasGeneratedKey) {
      ids = new Object[pkeys.length + 1];
      keys = new String[pkeys.length + 1];
      keys[j++] = generatedKey;
      ids[i++] = id;
    } else {
      ids = new Object[pkeys.length];
      keys = new String[pkeys.length];
    }
    for (String pKey : pkeys) {
      keys[j++] = pKey;
      ids[i++] = get(pKey);
    }
    if (ids.length > 0) {
      params = new Object[ids.length + values.length];
      System.arraycopy(values, 0, params, 0, values.length);
      System.arraycopy(ids, 0, params, values.length, ids.length);
      where = Joiner.on("=? AND ").join(keys) + "=?";
    } else {
      params = values;
    }

    String sql = dialect.update(tableMeta.getTableName(), getAlias(), where, columns);
    if (update(sql, params)) {
      clearModifyAttrs();
      return true;
    }
    return false;
  }

  /**
   * Execute sql update
   */
  public boolean execute(String... sqls) {
    return execute(Arrays.asList(sqls));
  }

  /**
   * Execute a batch of SQL INSERT, UPDATE, or DELETE queries.
   *
   * @param sqls The SQL list to execute.
   * @return The number of rows updated per statement
   */
  public boolean execute(List<String> sqls) {

    DataSourceMeta dsm = getDataSourceMeta();
    boolean showSql = dsm.isWriteShowSql();
    Statement stmt = null;
    int[] result = null;
    Connection conn = null;
    Boolean autoCommit = null;
    try {
      conn = getWriteConnection(dsm);
      autoCommit = conn.getAutoCommit();
      if (autoCommit)
        conn.setAutoCommit(false);

      stmt = getPreparedStatement(showSql, conn, sqls);
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
   * Delete model.
   */
  public boolean delete() {
    Object[] ids = getPrimaryValues(getTableMeta());
    return deleteByIds(ids);
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
    String key = getPrimaryKey(tableMeta);
    String sql = getDialect().delete(tableMeta.getTableName(), key + "=?");
    return update(sql, id);
  }

  /**
   * Delete model by multi id
   *
   * @param ids
   * @return
   */
  public boolean deleteByIds(Object... ids) {
    checkNotNull(ids, "You can't delete model without primaryKey.");
    TableMeta tableMeta = getTableMeta();
    String[] keys = getPrimaryKeys(tableMeta);
    String sql = getDialect().delete(tableMeta.getTableName(), Joiner.on("=? AND ").join(keys) + "=?");
    return update(sql, ids);
  }


  /**
   * Delete model by ids
   *
   * @param ids
   * @return
   */
  public boolean deleteInIds(Object... ids) {
    checkNotNull(ids, "You can't delete model without primaryKey.");
    TableMeta tableMeta = getTableMeta();
    String key = getPrimaryKey(tableMeta);
    Dialect dialect = getDialect();
    StringBuilder appendQuestions = new StringBuilder();
    for (int i = 0; i < ids.length; i++) {
      if (i == 0) {
        appendQuestions.append("?");
      } else {
        appendQuestions.append(",?");
      }
    }

    String sql = dialect.delete(tableMeta.getTableName(), key + " IN (" + appendQuestions + ")");
    return update(sql, ids);
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
   * 分页查询
   *
   * @param pageNumber 页码
   * @param pageSize   每页大小
   * @return 分页对象
   */
  public FullPage<M> fullPaginateAll(int pageNumber, int pageSize) {
    return fullPaginate(pageNumber, pageSize, getDialect().select(getTableMeta().getTableName()));
  }

  /**
   * 分页查询
   *
   * @param pageNumber 页码
   * @param pageSize   每页大小
   * @param columns    列 用逗号分割
   * @return 分页对象
   */
  public FullPage<M> fullPaginateColsAll(int pageNumber, int pageSize, String columns) {
    return fullPaginate(pageNumber, pageSize, getDialect().select(getTableMeta().getTableName(), columns.split(",")));
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
  public FullPage<M> fullPaginateBy(int pageNumber, int pageSize, String where, Object... params) {
    return fullPaginate(pageNumber, pageSize, getDialect().select(getTableMeta().getTableName(), getAlias(), where), params);
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
  public FullPage<M> fullPaginateColsBy(int pageNumber, int pageSize, String columns, String where, Object... params) {
    return fullPaginate(pageNumber, pageSize, getDialect().select(getTableMeta().getTableName(), getAlias(), where, columns.split(",")), params);
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
    return new Long(queryFirst(getDialect().count(getTableMeta().getTableName())).toString());
  }

  /**
   * COUNT 根据条件函数求和
   *
   * @return Long
   */
  public Long countBy(String where, Object... params) {
    return new Long(queryFirst(getDialect().count(getTableMeta().getTableName(), getAlias(), where), params).toString());
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

    boolean useCache = isUseCache();
    TableMeta tableMeta = getTableMeta();

    List<T> result = null;
    if (useCache) {
      //hit cache
      result = getCache(sql, params);
      if (result != null) {
        return result;
      }
    } else {
      logger.debug("This query not use cache.");
    }

    DataSourceMeta dsm = getDataSourceMeta();
    boolean showSql = dsm.isReadShowSql();
    Connection conn = null;
    PreparedStatement pst = null;
    ResultSet rs = null;

    try {
      conn = getWriteConnection(dsm);
      pst = getPreparedStatement(showSql, false, conn, tableMeta, sql, params);
      rs = pst.executeQuery();
      result = readQueryResult(rs);
    } catch (SQLException e) {
      throw new DBException(e.getMessage(), e);
    } finally {
      dsm.close(rs, pst, conn);
    }
    //add cache
    addCache(sql, params, result);
    return result;
  }

  /**
   * 读取result list
   *
   * @param rs  rs
   * @param <T> T
   * @return list
   * @throws SQLException
   */
  private <T> List<T> readQueryResult(ResultSet rs) throws SQLException {
    List<T> result = new ArrayList<T>();
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
    TableMeta tableMeta = getTableMeta();
    List<T> result = query(tableMeta.getDialect().paginateWith(1, 1, sql), params);
    return result.size() > 0 ? result.get(0) : null;
  }

  /**
   * 返回单个对象的存储过程
   *
   * @param sql
   * @param objectCall
   * @param <T>
   * @return
   */
  public <T> T queryCall(String sql, ObjectCall objectCall) {
    Connection conn = null;
    CallableStatement cstmt = null;
    DataSourceMeta dsm = getDataSourceMeta();
    try {
      conn = getWriteConnection(dsm);
      cstmt = conn.prepareCall(sql);
      return (T) objectCall.call(cstmt);
    } catch (SQLException e) {
      throw new DBException(e.getMessage(), e);
    } finally {
      dsm.close(cstmt, conn);
    }
  }


  /**
   * 调用存储过程
   * int CallableStatement.executeUpdate: 存储过程不返回结果集。
   * ResultSet CallableStatement.executeQuery: 存储过程返回一个结果集。
   * Boolean CallableStatement.execute: 存储过程返回多个结果集。
   * int[] CallableStatement.executeBatch: 提交批处理命令到数据库执行。
   *
   * @param sql           存储过程的sql
   * @param resultSetCall 执行请求  返回结果
   * @param <T>           返回类型
   * @return T
   */
  public <T> List<T> queryCall(String sql, ResultSetCall resultSetCall) {
    Connection conn = null;
    CallableStatement cstmt = null;
    DataSourceMeta dsm = getDataSourceMeta();
    try {
      conn = getWriteConnection(dsm);
      cstmt = conn.prepareCall(sql);
      return readQueryResult(resultSetCall.call(cstmt));
    } catch (SQLException e) {
      throw new DBException(e.getMessage(), e);
    } finally {
      dsm.close(cstmt, conn);
    }
  }

  /**
   * @param sql
   * @param resultSetCall
   * @param <T>
   * @return
   */
  public <T> T queryCallFirst(String sql, ResultSetCall resultSetCall) {
    TableMeta tableMeta = getTableMeta();
    List<T> result = queryCall(tableMeta.getDialect().paginateWith(1, 1, sql), resultSetCall);
    return result.size() > 0 ? result.get(0) : null;
  }

  /**
   * 返回一个Model的结果集
   *
   * @param sql
   * @param resultSetCall
   * @return
   */
  public List<M> findCall(String sql, ResultSetCall resultSetCall) {
    Connection conn = null;
    CallableStatement cstmt = null;
    DataSourceMeta dsm = getDataSourceMeta();
    TableMeta tableMeta = getTableMeta();

    try {
      conn = getWriteConnection(dsm);
      cstmt = conn.prepareCall(sql);
      return BaseBuilder.build(resultSetCall.call(cstmt), getMClass(), dsm, tableMeta);
    } catch (SQLException e) {
      throw new DBException(e.getMessage(), e);
    } catch (InstantiationException e) {
      throw new EntityException(e.getMessage(), e);
    } catch (IllegalAccessException e) {
      throw new EntityException(e.getMessage(), e);
    } finally {
      dsm.close(cstmt, conn);
    }
  }

  /**
   * 返回一个Model
   *
   * @param sql
   * @param resultSetCall
   * @return
   */
  public M findCallFirst(String sql, ResultSetCall resultSetCall) {
    TableMeta tableMeta = getTableMeta();
    List<M> result = findCall(tableMeta.getDialect().paginateWith(1, 1, sql), resultSetCall);
    return result.size() > 0 ? result.get(0) : null;
  }

  /**
   * 反序列化的扩展类
   */
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    //注意这里的接受顺序是有限制的哦，否则的话会出错的
    //例如上面先write的是A对象的话，那么下面先接受的也一定是A对象...
    putAttrs((Map<String, Object>) in.readObject());
  }

  /**
   * 序列化操作的扩展类
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    //增加一个新的对象
    out.writeObject(getAttrs());
  }

}
