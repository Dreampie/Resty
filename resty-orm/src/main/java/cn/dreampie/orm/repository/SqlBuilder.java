package cn.dreampie.orm.repository;

import cn.dreampie.common.util.Joiner;
import cn.dreampie.log.Logger;
import cn.dreampie.orm.cache.QueryCache;
import cn.dreampie.orm.callback.Callback;
import cn.dreampie.orm.callback.ListMapback;
import cn.dreampie.orm.callback.Resultback;
import cn.dreampie.orm.exception.DBException;
import cn.dreampie.orm.meta.DataSourceMeta;
import cn.dreampie.orm.meta.EntityMeta;

import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Dreampie
 * @date 2015-06-17
 * @what
 */
public class SqlBuilder implements Serializable {
  protected final Logger logger = Logger.getLogger(SqlBuilder.class);

  protected final Pattern namePattern = Pattern.compile(":[a-zA-Z0-9_]+[^a-zA-Z0-9_]",
      Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

  protected final String SPACE = " ";

  protected StringBuilder sql = new StringBuilder();
  protected List<String> names = new ArrayList<String>();
  protected List<Object> params = new ArrayList<Object>();

  protected EntityMeta entityMeta;

  public SqlBuilder(EntityMeta entityMeta) {
    this.entityMeta = entityMeta;
  }

  public SqlBuilder build(String sql) {
    exlain(sql);
    return this;
  }

  private String exlain(String sql) {
    if (!sql.endsWith(SPACE)) {
      sql += SPACE;
    }

    Matcher nameMatcher = namePattern.matcher(sql);
    String nameGroup;
    int nameGroupLength;

    while (nameMatcher.find()) {
      nameGroup = nameMatcher.group();
      nameGroupLength = nameGroup.length();

      sql = sql.replace(nameGroup.substring(0, nameGroupLength - 1), "?");
      this.names.add(nameGroup.substring(1, nameGroupLength - 1));
    }
    this.sql = new StringBuilder(sql);
    return sql;
  }

  public SqlBuilder leftBracket() {
    this.sql.append(" (");
    return this;
  }

  public SqlBuilder rightBracket() {
    this.sql.append(") ");
    return this;
  }

  public SqlBuilder union() {
    this.sql.append("UNION ");
    return this;
  }

  public SqlBuilder select(String... columns) {
    this.sql.append("SELECT ");
    this.sql.append(Joiner.on(",").join(columns)).append(SPACE);
    return this;
  }

  public SqlBuilder select(List<String> columns) {
    this.sql.append("SELECT ");
    this.sql.append(Joiner.on(",").join(columns)).append(SPACE);
    return this;
  }

  public SqlBuilder from(String... tables) {
    this.sql.append("FROM ");
    this.sql.append(Joiner.on(",").join(tables)).append(SPACE);
    return this;
  }

  public SqlBuilder from(List<String> tables) {
    this.sql.append("FROM ");
    this.sql.append(Joiner.on(",").join(tables)).append(SPACE);
    return this;
  }

  public SqlBuilder join(String table) {
    this.sql.append("JOIN ").append(table).append(SPACE);
    return this;
  }

  public SqlBuilder innerJoin(String table) {
    this.sql.append("INNER JOIN ").append(table).append(SPACE);
    return this;
  }

  public SqlBuilder leftJoin(String table) {
    this.sql.append("LEFT JOIN ").append(table).append(SPACE);
    return this;
  }

  public SqlBuilder rightJoin(String table) {
    this.sql.append("RIGHT JOIN ").append(table).append(SPACE);
    return this;
  }

  public SqlBuilder on(String on) {
    this.sql.append("ON ").append(on).append(SPACE);
    return this;
  }

  public SqlBuilder where() {
    this.sql.append("WHERE ");
    return this;
  }

  public SqlBuilder where(String where) {
    this.sql.append("WHERE ").append(where).append(SPACE);
    return this;
  }

  public SqlBuilder and(String... term) {
    this.sql.append(Joiner.on("AND ").join(term)).append(SPACE);
    return this;
  }

  public SqlBuilder and(List<String> term) {
    this.sql.append(Joiner.on("AND ").join(term)).append(SPACE);
    return this;
  }

  public SqlBuilder or(String... term) {
    this.sql.append(Joiner.on("OR ").join(term)).append(SPACE);
    return this;
  }

  public SqlBuilder or(List<String> term) {
    this.sql.append(Joiner.on("OR ").join(term)).append(SPACE);
    return this;
  }

  public SqlBuilder group(String... columns) {
    this.sql.append("GROUP BY ");
    this.sql.append(Joiner.on(",").join(columns)).append(SPACE);
    return this;
  }

  public SqlBuilder group(List<String> columns) {
    this.sql.append("GROUP BY ");
    this.sql.append(Joiner.on(",").join(columns)).append(SPACE);
    return this;
  }

  public SqlBuilder order() {
    this.sql.append("ORDER BY ");
    return this;
  }

  public SqlBuilder asc(String... columns) {
    this.sql.append(Joiner.on(" ASC, ").join(columns));
    return this;
  }

  public SqlBuilder asc(List<String> columns) {
    this.sql.append(Joiner.on(" ASC, ").join(columns));
    return this;
  }

  public SqlBuilder desc(String... columns) {
    this.sql.append(Joiner.on(" DESC, ").join(columns));
    return this;
  }

  public SqlBuilder desc(List<String> columns) {
    this.sql.append(Joiner.on(" DESC, ").join(columns));
    return this;
  }

  public SqlBuilder having() {
    this.sql.append("HAVING ");
    return this;
  }

  public SqlBuilder isnull(String column) {
    this.sql.append(column).append(" IS NULL");
    return this;
  }

  public SqlBuilder isnotnull(String column) {
    this.sql.append(column).append(" IS NOT NULL");
    return this;
  }

  public SqlBuilder update(String table) {
    this.sql.append("UPDATE ").append(table).append(SPACE);
    return this;
  }

  public SqlBuilder set(String... sets) {
    this.sql.append(Joiner.on(", ").join(sets)).append(SPACE);
    return this;
  }

  public SqlBuilder set(List<String> sets) {
    this.sql.append(Joiner.on(", ").join(sets)).append(SPACE);
    return this;
  }

  public SqlBuilder delete(String table) {
    this.sql.append("DELETE FROM ").append(table).append(SPACE);
    return this;
  }

  public SqlBuilder insert(String table) {
    this.sql.append("INSERT INTO ").append(table).append(SPACE);
    return this;
  }

  public SqlBuilder columns(String... columns) {
    this.sql.append(Joiner.on(", ").join(columns)).append(SPACE);
    return this;
  }

  public SqlBuilder columns(List<String> columns) {
    this.sql.append(Joiner.on(", ").join(columns)).append(SPACE);
    return this;
  }

  public SqlBuilder values(String... values) {
    this.sql.append("VALUES(").append(Joiner.on(", ").join(values)).append(")").append(SPACE);
    return this;
  }

  public SqlBuilder values(List<String> values) {
    this.sql.append("VALUES(").append(Joiner.on(", ").join(values)).append(")").append(SPACE);
    return this;
  }

  public SqlBuilder values(int len) {
    this.sql.append("VALUES(");
    for (int i = 0; i < len; i++) {
      this.sql.append(i > 0 ? ", ?" : "?");
    }
    this.sql.append(")").append(SPACE);
    return this;
  }

  public SqlBuilder limit(int start, int size) {
    this.sql.append("LIMIT ").append(start).append(", ").append(size);
    return this;
  }

  public SqlBuilder limit(int size) {
    this.sql.append("LIMIT ").append(", ").append(size);
    return this;
  }

  public SqlBuilder add(Object... params) {
    Collections.addAll(this.params, params);
    return this;
  }

  public SqlBuilder add(List<Object> params) {
    this.params.addAll(params);
    return this;
  }

  public SqlBuilder add(String name, Object value) {
    exlain(this.sql.toString());
    this.params.add(names.indexOf(name), value);
    return this;
  }

  public SqlBuilder add(Map<String, Object> params) {
    exlain(this.sql.toString());
    int i = 0;
    for (String name : names) {
      this.params.add(i++, params.get(name));
    }
    return this;
  }

  public <T> T query() {
    return query((Resultback) null);
  }

  public <T> T query(Resultback resultback) {
    T result = null;

    DataSourceMeta dataSourceMeta = entityMeta.getDataSourceMeta();

    Connection connection = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    try {
      connection = dataSourceMeta.getConnection();
      boolean cached = entityMeta.isCached();
      //hit cache
      if (cached) {
        result = QueryCache.instance().get(entityMeta.getDsName(), entityMeta.getTable(), entityMeta.getVersion(), sql.toString(), params.toArray());
      }
      if (result != null) {
        return result;
      }

      //excute query
      pst = getPreparedStatement(connection, entityMeta, sql.toString(), params.toArray());
      rs = pst.executeQuery();
      if (resultback != null) {
        result = (T) resultback.call(rs);
      } else {
        result = (T) new ListMapback().call(rs);
      }
      //hit cache
      if (cached) {
        QueryCache.instance().add(entityMeta.getDsName(), entityMeta.getTable(), entityMeta.getVersion(), sql.toString(), params.toArray(), result);
      }
    } catch (SQLException e) {
      throw new DBException(e.getMessage(), e);
    } finally {
      dataSourceMeta.close(rs, pst, connection);
    }
    return result;
  }


  public int update() {
    DataSourceMeta dataSourceMeta = entityMeta.getDataSourceMeta();

    Connection connection = null;
    PreparedStatement pst = null;
    int result;
    try {
      connection = dataSourceMeta.getConnection();
      pst = getPreparedStatement(connection, entityMeta, sql.toString(), params.toArray());
      result = pst.executeUpdate();

      boolean cached = entityMeta.isCached();
      //hit cache
      if (cached) {
        QueryCache.instance().purge(entityMeta.getDsName(), entityMeta.getTable(), entityMeta.getVersion());
      }
    } catch (SQLException e) {
      throw new DBException(e.getMessage(), e);
    } finally {
      dataSourceMeta.close(pst, connection);
    }
    return result;
  }

  public <T> T query(Callback callback) {
    return query(callback, null);
  }

  public <T> T query(Callback callback, Resultback resultback) {
    T result = null;
    boolean cached = entityMeta.isCached();
    //hit cache
    if (cached) {
      result = QueryCache.instance().get(entityMeta.getDsName(), entityMeta.getTable(), entityMeta.getVersion(), sql.toString(), params.toArray());
    }
    if (result != null) {
      return result;
    }
    DataSourceMeta dataSourceMeta = entityMeta.getDataSourceMeta();

    Connection connection = null;
    CallableStatement cstmt = null;
    try {
      connection = dataSourceMeta.getConnection();
      cstmt = connection.prepareCall(sql.toString());
      if (resultback == null) {
        result = callback.call(cstmt);
      } else {
        result = (T) resultback.call(callback.<ResultSet>call(cstmt));
      }
      //hit cache
      if (cached) {
        QueryCache.instance().add(entityMeta.getDsName(), entityMeta.getTable(), entityMeta.getVersion(), sql.toString(), params.toArray(), result);
      }
    } catch (SQLException e) {
      throw new DBException(e.getMessage(), e);
    } finally {
      dataSourceMeta.close(cstmt, connection);
    }
    return result;
  }

  public <T> T update(Callback callback) {
    T result = null;

    DataSourceMeta dataSourceMeta = entityMeta.getDataSourceMeta();
    Connection connection = null;
    CallableStatement cstmt = null;
    try {
      connection = dataSourceMeta.getConnection();
      cstmt = connection.prepareCall(sql.toString());
      result = callback.call(cstmt);

      boolean cached = entityMeta.isCached();
      //hit cache
      if (cached) {
        QueryCache.instance().purge(entityMeta.getDsName(), entityMeta.getTable(), entityMeta.getVersion());
      }
    } catch (SQLException e) {
      throw new DBException(e.getMessage(), e);
    } finally {
      dataSourceMeta.close(cstmt, connection);
    }
    return result;
  }


  /**
   * 获取sql执行对象
   *
   * @param connection
   * @param entityMeta
   * @param sql
   * @param params
   * @return
   * @throws SQLException
   */
  protected PreparedStatement getPreparedStatement(Connection connection, EntityMeta entityMeta, String sql, Object[] params) throws SQLException {
    //打印sql语句
    logSql(sql, params);
    PreparedStatement pst;
    //如果没有自动生成的主键 则不获取
    String id = entityMeta.getIdMeta().getColumn();
    GenerateType generateType = entityMeta.getIdMeta().getGenerate();
    if (generateType == GenerateType.AUTO) {
      pst = connection.prepareStatement(sql, new String[]{id});
    } else {
      pst = connection.prepareStatement(sql);
    }
    for (int i = 0; i < params.length; i++) {
      pst.setObject(i + 1, params[i]);
    }
    return pst;
  }


  /**
   * sql语句
   *
   * @param sql    sql
   * @param params 参数
   */
  protected void logSql(String sql, Object[][] params) {
    if (entityMeta.getDataSourceMeta().isShowSql() && logger.isInfoEnabled()) {
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
  protected void logSql(String sql, Object[] params) {
    if (entityMeta.getDataSourceMeta().isShowSql() && logger.isInfoEnabled()) {
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
  protected void logSql(List<String> sqls) {
    if (entityMeta.getDataSourceMeta().isShowSql() && logger.isInfoEnabled()) {
      logger.info("Sqls: " + '{' + Joiner.on("}, {").useForNull("null").join(sqls) + '}');
    }
  }


}
