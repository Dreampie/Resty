package cn.dreampie.orm.repository;

import cn.dreampie.common.entity.Attrs;
import cn.dreampie.common.util.Joiner;
import cn.dreampie.log.Logger;
import cn.dreampie.orm.DataSourceMeta;
import cn.dreampie.orm.Metadata;
import cn.dreampie.orm.TableMeta;
import cn.dreampie.orm.cache.QueryCache;
import cn.dreampie.orm.dialect.Dialect;
import cn.dreampie.orm.exception.DBException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.List;

/**
 * @author Dreampie
 * @date 2015-06-14
 * @what
 */
public class BaseRepository<E> {

  private final Logger logger = Logger.getLogger(getClass());

  /**
   * 获取实体Class
   *
   * @return
   */
  public Class<E> getEntityClass() {
    Class clazz = getClass();
    Type[] actualTypeArguments = ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments();
    if (actualTypeArguments.length > 0) {
      return (Class<E>) actualTypeArguments[0];
    } else {
      throw new DBException("Could not found entity.");
    }
  }

  /**
   * 获取table元数据
   *
   * @return
   */
  public TableMeta getTableMeta() {
    Class<E> entityClass = getEntityClass();
    return Metadata.getTableMeta(entityClass);
  }

  /**
   * 获取数据源元数据
   *
   * @return
   */
  public DataSourceMeta getDataSourceMeta() {
    TableMeta tableMeta = getTableMeta();
    return Metadata.getDataSourceMeta(tableMeta.getDsName());
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
   * 判断是否是表的属性
   *
   * @param column 属性名
   * @return
   */
  public boolean hasColumn(String column) {
    return getTableMeta().hasColumn(column);
  }

  /**
   * 获取改数据库列对应的java类型
   *
   * @param column 属性名
   * @return class
   */
  public Class getColumnType(String column) {
    return getDialect().getColumnType(getTableMeta().getDataType(column));
  }


  /**
   * sql连接对象
   *
   * @return Connection
   * @throws SQLException
   */
  private Connection getConnection(DataSourceMeta dataSourceMeta) throws SQLException {
    dataSourceMeta.beginTransaction();
    return dataSourceMeta.getConnection();
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
  private PreparedStatement getPreparedStatement(Connection conn, TableMeta tableMeta, String sql, Object[] params) throws SQLException {
    //打印sql语句
    logSql(sql, params);
    PreparedStatement pst;
    //如果没有自动生成的主键 则不获取
    String generatedKey = tableMeta.getId();
    GenerateType generateType = tableMeta.getGenerateType();
    if (generateType == GenerateType.AUTO) {
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
  private PreparedStatement getPreparedStatement(Connection conn, TableMeta tableMeta, String sql, Object[][] params) throws SQLException {
    //打印sql语句
    logSql(sql, params);

    PreparedStatement pst = null;
    //如果没有自动生成的主键 则不获取
    String generatedKey = tableMeta.getId();
    GenerateType generateType = tableMeta.getGenerateType();
    if (generateType == GenerateType.AUTO) {
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
  protected void setGeneratedKey(PreparedStatement pst, TableMeta tableMeta,Object entity) throws SQLException {
    String generatedKey = tableMeta.getId();
    GenerateType generateType = tableMeta.getGenerateType();
    if (generateType == GenerateType.AUTO) {
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
  protected void setGeneratedKey(PreparedStatement pst, TableMeta tableMeta, List<?> entities) throws SQLException {
    String generatedKey = tableMeta.getId();
    GenerateType generateType = tableMeta.getGenerateType();
    if (generateType == GenerateType.AUTO) {
      ResultSet rs = pst.getGeneratedKeys();
      for (Object entity : entities) {
        if (Attributor == null) {
          if (rs.next()) {
            entity.set(generatedKey, rs.getObject(1));
          }
        }
      }
      rs.close();
    }
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
      return (T) QueryCache.instance().get(tableMeta.getDsName(), tableMeta.getTableName(), sql, params);
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
      QueryCache.instance().add(tableMeta.getDsName(), tableMeta.getTableName(), sql, params, cache);
    }
  }

  /**
   * 清除缓存 通过数据源名称＋表名称
   */
  public void purgeCache() {
    TableMeta tableMeta = getTableMeta();
    if (tableMeta.isCached()) {
      QueryCache.instance().purge(tableMeta.getDsName(), tableMeta.getTableName());
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
      QueryCache.instance().remove(tableMeta.getDsName(), tableMeta.getTableName(), sql, params);
    }
  }


  /**
   * sql语句
   *
   * @param sql    sql
   * @param params 参数
   */
  private void logSql(String sql, Object[][] params) {
    if (getDataSourceMeta().isShowSql() && logger.isInfoEnabled()) {
      StringBuilder log = new StringBuilder("Sql: {").append(sql).append("} ");
      if (params != null && params.length > 0) {
        for (Object[] para : params) {
          log.append(", params: ").append('{');
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
  private void logSql(String sql, Object[] params) {
    if (getDataSourceMeta().isShowSql() && logger.isInfoEnabled()) {
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
  private void logSql(List<String> sqls) {
    if (getDataSourceMeta().isShowSql() && logger.isInfoEnabled()) {
      logger.info("Sqls: " + '{' + Joiner.on("}, {").useForNull("null").join(sqls) + '}');
    }
  }

}
