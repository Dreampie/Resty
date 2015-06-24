package cn.dreampie.orm.repository;

import cn.dreampie.orm.exception.DBException;
import cn.dreampie.orm.meta.DataSourceMeta;
import cn.dreampie.orm.meta.EntityMeta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author Dreampie
 * @date 2015-06-17
 * @what
 */
public class BatchSqlBuilder extends SqlBuilder {

  public BatchSqlBuilder(EntityMeta entityMeta) {
    super(entityMeta);
  }

  public SqlBuilder add(Object[]... params) {
    return super.add(params);
  }

  public int[] execute() {

    DataSourceMeta dataSourceMeta = entityMeta.getDataSourceMeta();
    Connection conn = null;
    PreparedStatement pst = null;
    Boolean autoCommit = null;
    int[] result = null;
    try {
      conn = dataSourceMeta.getConnection();
      autoCommit = conn.getAutoCommit();
      if (autoCommit) {
        conn.setAutoCommit(false);
      }
      pst = getPreparedStatement(conn, entityMeta, sql.toString(), (Object[][]) params.toArray());
      result = pst.executeBatch();
      //没有事务的情况下 手动提交
      if (dataSourceMeta.getCurrentConnection() == null)
        conn.commit();
      conn.setAutoCommit(autoCommit);
    } catch (SQLException e) {
      throw new DBException(e.getMessage(), e);
    } finally {
      dataSourceMeta.close(pst, conn);
    }
    return result;
  }


  public int[] execute(List<String> sqls) {

    Statement stmt = null;
    int[] result = null;
    Connection conn = null;
    Boolean autoCommit = null;
    DataSourceMeta dataSourceMeta = entityMeta.getDataSourceMeta();
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
    } catch (SQLException e) {
      throw new DBException(e.getMessage(), e);
    } finally {
      dataSourceMeta.close(stmt, conn);
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
  private PreparedStatement getPreparedStatement(Connection connection, EntityMeta entityMeta, String sql, Object[][] params) throws SQLException {
    //打印sql语句
    logSql(sql, params);

    PreparedStatement pst = null;
    //如果没有自动生成的主键 则不获取
    String id = entityMeta.getIdMeta().getColumn();
    GenerateType generateType = entityMeta.getIdMeta().getGenerate();
    if (generateType == GenerateType.AUTO) {
      String[] returnKeys = new String[params.length];
      for (int i = 0; i < params.length; i++) {
        returnKeys[i] = id;
      }
      pst = connection.prepareStatement(sql, returnKeys);
    } else {
      pst = connection.prepareStatement(sql);
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
   * @param connection
   * @param sqls
   * @return
   * @throws SQLException
   */
  private Statement getPreparedStatement(Connection connection, List<String> sqls) throws SQLException {
    //打印sql语句
    logSql(sqls);
    Statement stmt = null;

    stmt = connection.createStatement();
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
}
