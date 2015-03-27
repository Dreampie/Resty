package cn.dreampie.orm;


import cn.dreampie.log.Logger;
import cn.dreampie.orm.exception.DBException;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cn.dreampie.common.util.Checker.checkNotNull;

/**
 * DS. Professional database query and update tool.
 */
public class DS {
  private static final Logger logger = Logger.getLogger(DS.class);

  public static final String DEFAULT_PRIMARY_KAY = "id";
  public static final Object[] NULL_PARA_ARRAY = new Object[0];
  private DataSourceMeta dataSourceMeta;

  private DS() {
  }

  public static DS use() {
    return DS.useDS(Metadata.getDefaultDsName());
  }

  public static DS useDS(String dsName) {
    return DS.useDS(Metadata.getDataSourceMeta(dsName));
  }

  public static DS useDS(DataSourceMeta dataSourceMeta) {
    checkNotNull(dataSourceMeta, "Could not found dataSourceMeta.");
    DS ds = new DS();
    ds.dataSourceMeta = dataSourceMeta;
    return ds;
  }

  public DataSourceMeta getDataSourceMeta() {
    return dataSourceMeta;
  }

  public static PreparedStatement getPreparedStatement(Connection conn, String primaryKey, String sql, Object[] paras) throws SQLException {
    PreparedStatement pst = conn.prepareStatement(sql, new String[]{primaryKey == null ? DEFAULT_PRIMARY_KAY : primaryKey});

    for (int i = 0; i < paras.length; i++) {
      pst.setObject(i + 1, paras[i]);
    }
    return pst;
  }


  public static PreparedStatement getPreparedStatement(Connection connection, String primaryKey, String sql, Object[][] paras) throws SQLException {
    PreparedStatement pst = null;
    String key = primaryKey == null ? DEFAULT_PRIMARY_KAY : primaryKey;
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

  private static Statement getPreparedStatement(Connection connection, List<String> sql) throws SQLException {
    Statement stmt = null;

    stmt = connection.createStatement();
    final int batchSize = 1000;
    int count = 0;
    int size = sql.size();
    for (int i = 0; i < size; i++) {
      stmt.addBatch(sql.get(i));
      if (++count % batchSize == 0) {
        stmt.executeBatch();
      }
    }
    return stmt;
  }


  public <T> List<T> query(String sql, Object... paras) {

    List result = new ArrayList();
    Connection conn = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    try {
      conn = dataSourceMeta.getConnection();
      pst = getPreparedStatement(conn, DEFAULT_PRIMARY_KAY, sql, paras);
      rs = pst.executeQuery();
      int colAmount = rs.getMetaData().getColumnCount();
      if (colAmount > 1) {
        while (rs.next()) {
          Object[] temp = new Object[colAmount];
          for (int i = 0; i < colAmount; i++) {
            temp[i] = rs.getObject(i + 1);
          }
          result.add(temp);
        }
      } else if (colAmount == 1) {
        while (rs.next()) {
          result.add(rs.getObject(1));
        }
      }
    } catch (SQLException e) {
      throw new DBException(e.getMessage(), e);
    } finally {
      dataSourceMeta.close(rs, pst, conn);
    }
    return result;
  }

  /**
   * @param sql an SQL statement
   * @see #query(String, Object...)
   */
  public <T> List<T> query(String sql) {    // return  List<object[]> or List<object>
    return query(sql, NULL_PARA_ARRAY);
  }

  /**
   * Execute sql query and return the first result. I recommend add "limit 1" in your sql.
   *
   * @param sql   an SQL statement that may contain one or more '?' IN parameter placeholders
   * @param paras the parameters of sql
   * @return Object[] if your sql has select more than one column,
   * and it return Object if your sql has select only one column.
   */
  public <T> T queryFirst(String sql, Object... paras) {
    List<T> result = query(sql, paras);
    return (result.size() > 0 ? result.get(0) : null);
  }

  /**
   * @param sql an SQL statement
   * @see #queryFirst(String, Object...)
   */
  public <T> T queryFirst(String sql) {
    // return queryFirst(sql, NULL_PARA_ARRAY);
    List<T> result = query(sql, NULL_PARA_ARRAY);
    return (result.size() > 0 ? result.get(0) : null);
  }

  // 26 queryXxx method below -----------------------------------------------

  /**
   * Execute sql query just return one column.
   *
   * @param <T>   the type of the column that in your sql's select statement
   * @param sql   an SQL statement that may contain one or more '?' IN parameter placeholders
   * @param paras the parameters of sql
   * @return List<T>
   */
  public <T> T queryColumn(String sql, Object... paras) {
    List<T> result = query(sql, paras);
    if (result.size() > 0) {
      T temp = result.get(0);
      if (temp instanceof Object[])
        throw new DBException("Only one column can be queried.");
      return temp;
    }
    return null;
  }

  public <T> T queryColumn(String sql) {
    return (T) queryColumn(sql, NULL_PARA_ARRAY);
  }

  public String queryStr(String sql, Object... paras) {
    return (String) queryColumn(sql, paras);
  }

  public String queryStr(String sql) {
    return (String) queryColumn(sql, NULL_PARA_ARRAY);
  }

  public Integer queryInt(String sql, Object... paras) {
    return (Integer) queryColumn(sql, paras);
  }

  public Integer queryInt(String sql) {
    return (Integer) queryColumn(sql, NULL_PARA_ARRAY);
  }

  public Long queryLong(String sql, Object... paras) {
    return (Long) queryColumn(sql, paras);
  }

  public Long queryLong(String sql) {
    return (Long) queryColumn(sql, NULL_PARA_ARRAY);
  }

  public Double queryDouble(String sql, Object... paras) {
    return (Double) queryColumn(sql, paras);
  }

  public Double queryDouble(String sql) {
    return (Double) queryColumn(sql, NULL_PARA_ARRAY);
  }

  public Float queryFloat(String sql, Object... paras) {
    return (Float) queryColumn(sql, paras);
  }

  public Float queryFloat(String sql) {
    return (Float) queryColumn(sql, NULL_PARA_ARRAY);
  }

  public java.math.BigDecimal queryBigDecimal(String sql, Object... paras) {
    return (java.math.BigDecimal) queryColumn(sql, paras);
  }

  public java.math.BigDecimal queryBigDecimal(String sql) {
    return (java.math.BigDecimal) queryColumn(sql, NULL_PARA_ARRAY);
  }

  public byte[] queryBytes(String sql, Object... paras) {
    return (byte[]) queryColumn(sql, paras);
  }

  public byte[] queryBytes(String sql) {
    return (byte[]) queryColumn(sql, NULL_PARA_ARRAY);
  }

  public java.util.Date queryDate(String sql, Object... paras) {
    return (java.util.Date) queryColumn(sql, paras);
  }

  public java.util.Date queryDate(String sql) {
    return (java.util.Date) queryColumn(sql, NULL_PARA_ARRAY);
  }

  public java.sql.Time queryTime(String sql, Object... paras) {
    return (java.sql.Time) queryColumn(sql, paras);
  }

  public java.sql.Time queryTime(String sql) {
    return (java.sql.Time) queryColumn(sql, NULL_PARA_ARRAY);
  }

  public java.sql.Timestamp queryTimestamp(String sql, Object... paras) {
    return (java.sql.Timestamp) queryColumn(sql, paras);
  }

  public java.sql.Timestamp queryTimestamp(String sql) {
    return (java.sql.Timestamp) queryColumn(sql, NULL_PARA_ARRAY);
  }

  public Boolean queryBoolean(String sql, Object... paras) {
    return (Boolean) queryColumn(sql, paras);
  }

  public Boolean queryBoolean(String sql) {
    return (Boolean) queryColumn(sql, NULL_PARA_ARRAY);
  }

  public Number queryNumber(String sql, Object... paras) {
    return (Number) queryColumn(sql, paras);
  }

  public Number queryNumber(String sql) {
    return (Number) queryColumn(sql, NULL_PARA_ARRAY);
  }
  // 26 queryXxx method under -----------------------------------------------

  /**
   * Execute sql update
   */
  public int update(String sql, Object... paras) {
    int result = -1;
    Connection conn = null;
    PreparedStatement pst = null;
    try {
      conn = dataSourceMeta.getConnection();
      pst = getPreparedStatement(conn, DEFAULT_PRIMARY_KAY, sql, paras);
      result = pst.executeUpdate();
    } catch (SQLException e) {
      throw new DBException(e.getMessage(), e);
    } finally {
      dataSourceMeta.close(pst, conn);
    }
    return result;
  }


  /**
   * @param sql an SQL statement
   * @see #update(String, Object...)
   */
  public int update(String sql) {
    return update(sql, NULL_PARA_ARRAY);
  }


  public boolean execute(String... sqls) {
    return execute(Arrays.asList(sqls));
  }

  /**
   * Execute a batch of SQL INSERT, UPDATE, or DELETE queries.
   * Example:
   * <pre>
   * int[] result = DbPro.use().batch("myConfig", sqlList, 500);
   * </pre>
   *
   * @param sqls The SQL list to execute.
   * @return The number of rows updated per statement
   */
  public boolean execute(List<String> sqls) {

    Statement stmt = null;
    int[] result = null;
    Connection conn = null;
    Boolean autoCommit = null;
    try {
      conn = dataSourceMeta.getConnection();
      autoCommit = conn.getAutoCommit();
      if (autoCommit)
        conn.setAutoCommit(false);

      stmt = getPreparedStatement(conn, sqls);
      result = stmt.executeBatch();
      //没有事务的情况下 手动提交
      if (dataSourceMeta.getCurrentConnection() == null)
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
      dataSourceMeta.close(stmt, conn);
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
    try {
      conn = dataSourceMeta.getConnection();
      cstmt = conn.prepareCall(sql);
      return (T) inCall.call(cstmt);
    } catch (SQLException e) {
      throw new DBException(e.getMessage(), e);
    } finally {
      dataSourceMeta.close(cstmt, conn);
    }
  }
}



