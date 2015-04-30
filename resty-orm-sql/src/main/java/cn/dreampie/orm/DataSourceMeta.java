package cn.dreampie.orm;

import cn.dreampie.log.Logger;
import cn.dreampie.orm.dialect.Dialect;
import cn.dreampie.orm.provider.DataSourceProvider;
import com.alibaba.druid.pool.DruidDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * ConnectionAccess
 */
public class DataSourceMeta {

  private static final Logger logger = Logger.getLogger(DataSourceMeta.class);
  //不能使用static 让每个数据源都有一个connectionTL
  private final ThreadLocal<Connection> connectionTL = new ThreadLocal<Connection>();
  private DataSourceProvider dataSourceProvider;

  public DataSourceMeta(DataSourceProvider dataSourceProvider) {
    this.dataSourceProvider = dataSourceProvider;
  }

  public String getDsName() {
    return dataSourceProvider.getDsName();
  }

  public DataSource getDataSource() {
    return dataSourceProvider.getDataSource();
  }

  public Dialect getDialect() {
    return dataSourceProvider.getDialect();
  }

  public boolean isShowSql() {
    return dataSourceProvider.isShowSql();
  }

  public Connection getConnection() throws SQLException {
    Connection conn = connectionTL.get();
    if (conn != null)
      return conn;
    return getDataSource().getConnection();
  }

  public Connection getCurrentConnection() {
    return connectionTL.get();
  }

  public void setCurrentConnection(Connection connection) {
    connectionTL.set(connection);
  }

  public void rmCurrentConnection() {
    connectionTL.remove();
  }

  public final void close() {
    DataSource dataSource = getDataSource();
    if (dataSource != null && dataSource instanceof DruidDataSource)
      ((DruidDataSource) dataSource).close();
  }

  /**
   * Close ResultSet、Statement、Connection
   * ThreadLocal support declare transaction.
   */
  public final void close(ResultSet rs, Statement st, Connection conn) {
    if (rs != null) {
      try {
        rs.close();
      } catch (SQLException e) {
        logger.warn("Could not close resultSet!", e);
      }
    }
    //关闭连接
    close(st, conn);
  }

  public final void close(Statement st, Connection conn) {
    if (st != null) {
      try {
        st.close();
      } catch (SQLException e) {
        logger.warn("Could not close statement!", e);
      }
    }
    //关闭连接
    close(conn);
  }

  public final void close(Connection conn) {
    if (connectionTL.get() == null) {   // in transaction if conn in threadlocal
      if (conn != null)
        try {
          conn.close();
        } catch (SQLException e) {
          logger.warn("Could not close connection!", e);
        }
    }
  }
}
