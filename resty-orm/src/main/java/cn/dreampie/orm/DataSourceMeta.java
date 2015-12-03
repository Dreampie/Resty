package cn.dreampie.orm;

import cn.dreampie.log.Logger;
import cn.dreampie.orm.dialect.Dialect;
import cn.dreampie.orm.exception.TransactionException;
import cn.dreampie.orm.provider.DataSourceProvider;

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
  //不能使用static 让每个数据源都有一个connectionHolder
  private final ThreadLocal<Connection> connectionHolder = new ThreadLocal<Connection>();
  private final ThreadLocal<TransactionManager> transactionManagerHolder = new ThreadLocal<TransactionManager>();
  private final ThreadLocal<Integer> transactionDeepHolder = new ThreadLocal<Integer>();
  private final String dsmName;
  private DataSourceProvider writeDataSourceProvider;
  private DataSourceProvider readDataSourceProvider;

  public DataSourceMeta(DataSourceProvider writeDataSourceProvider) {
    this(writeDataSourceProvider.getDsName(), writeDataSourceProvider, null);
  }

  public DataSourceMeta(String dsmName, DataSourceProvider writeDataSourceProvider, DataSourceProvider readDataSourceProvider) {
    this.dsmName = dsmName;
    this.writeDataSourceProvider = writeDataSourceProvider;
    this.readDataSourceProvider = readDataSourceProvider;

    if (readDataSourceProvider != null && !writeDataSourceProvider.getDialect().equals(readDataSourceProvider.getDialect())) {
      throw new IllegalArgumentException("Different read and write database dialect");
    }
  }

  public String getDsmName() {
    return dsmName;
  }

  public Dialect getDialect() {
    return writeDataSourceProvider.getDialect();
  }

  public String getWriteDsName() {
    return writeDataSourceProvider.getDsName();
  }

  DataSource getWriteDataSource() {
    return writeDataSourceProvider.getDataSource();
  }

  public boolean isWriteShowSql() {
    return writeDataSourceProvider.isShowSql();
  }

  public String getReadDsName() {
    if (readDataSourceProvider != null) {
      return readDataSourceProvider.getDsName();
    } else {
      return getWriteDsName();
    }
  }

  DataSource getReadDataSource() {
    if (readDataSourceProvider != null) {
      return readDataSourceProvider.getDataSource();
    } else {
      return getWriteDataSource();
    }
  }

  public boolean isReadShowSql() {
    if (readDataSourceProvider != null) {
      return readDataSourceProvider.isShowSql();
    } else {
      return isWriteShowSql();
    }
  }

  /**
   * 获取连接对象
   *
   * @return 连接对象
   * @throws SQLException
   */
  public Connection getWriteConnection() throws SQLException {
    Connection conn = connectionHolder.get();
    if (conn != null) {
      return conn;
    }
    return getWriteDataSource().getConnection();
  }

  public Connection getReadConnection() throws SQLException {
    Connection conn = connectionHolder.get();
    if (conn != null) {
      return conn;
    }
    if (readDataSourceProvider != null) {
      return getReadDataSource().getConnection();
    } else {
      return getWriteDataSource().getConnection();
    }
  }

  /**
   * 当前连接对象
   *
   * @return connection
   */
  Connection getCurrentConnection() {
    return connectionHolder.get();
  }

  /**
   * 设置当前连接对象
   *
   * @param connection connection
   */
  void setCurrentConnection(Connection connection) {
    connectionHolder.set(connection);
  }

  /**
   * 移除连接对象
   */
  void rmCurrentConnection() {
    connectionHolder.remove();
  }

  /**
   * 初始化事务对象
   */
  public void initTransaction(boolean readonly, int level) {
    if (transactionManagerHolder.get() == null) {
      transactionManagerHolder.set(new TransactionManager(this, readonly, level));
      transactionDeepHolder.set(1);
    } else {
      transactionDeepHolder.set(transactionDeepHolder.get() + 1);
    }
  }

  /**
   * 开始事务
   *
   * @throws TransactionException
   */
  public void beginTransaction() throws TransactionException {
    TransactionManager transactionManager = transactionManagerHolder.get();
    //当前事务管理对象
    if (transactionManager != null && !transactionManager.isBegined()) {
      transactionManager.begin();
    }
  }

  /**
   * 提交事务
   *
   * @throws TransactionException
   */
  public void commitTransaction() throws TransactionException {
    if (transactionDeepHolder.get() == 1) {
      TransactionManager transactionManager = transactionManagerHolder.get();
      if (transactionManager != null) {
        transactionManager.commit();
      }
    }
  }

  /**
   * 回滚事务
   *
   * @throws TransactionException
   */
  public void rollbackTransaction() {
    if (transactionDeepHolder.get() == 1) {
      TransactionManager transactionManager = transactionManagerHolder.get();
      if (transactionManager != null) {
        transactionManager.rollback();
      }
    }
  }

  /**
   * 结束事务
   *
   * @throws TransactionException
   */
  public void endTranasaction() {
    if (transactionDeepHolder.get() == 1) {
      TransactionManager transactionManager = transactionManagerHolder.get();
      if (transactionManager != null) {
        transactionManager.end();
      }
      transactionManagerHolder.remove();
    } else {
      transactionDeepHolder.set(transactionDeepHolder.get() - 1);
    }
  }

  /**
   * 关闭数据源
   */
  public final void close() {
    writeDataSourceProvider.close();
    if (readDataSourceProvider != null) {
      readDataSourceProvider.close();
    }
  }

  /**
   * 关ResultSet闭结果级对象
   *
   * @param rs   ResultSet
   * @param st   Statement
   * @param conn Connection
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

  /**
   * 关闭Statement
   *
   * @param st   Statement
   * @param conn Connection
   */
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

  /**
   * 关闭Connection
   *
   * @param conn Connection
   */
  public final void close(Connection conn) {
    if (connectionHolder.get() == null) {   // in transaction if conn in threadlocal
      if (conn != null)
        try {
          conn.close();
        } catch (SQLException e) {
          logger.warn("Could not close connection!", e);
        }
    }
  }
}
