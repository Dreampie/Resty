package cn.dreampie.orm;

import cn.dreampie.log.Logger;
import cn.dreampie.orm.dialect.Dialect;
import cn.dreampie.orm.exception.TransactionException;
import cn.dreampie.orm.provider.DataSourceProvider;
import cn.dreampie.orm.transaction.TransactionManager;

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
  private final ThreadLocal<TransactionManager> transactionManagerTL = new ThreadLocal<TransactionManager>();
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

  /**
   * 获取连接对象
   *
   * @return 连接对象
   * @throws SQLException
   */
  public Connection getConnection() throws SQLException {
    Connection conn = connectionTL.get();
    if (conn != null) {
      return conn;
    }
    return getDataSource().getConnection();
  }

  /**
   * 当前连接对象
   *
   * @return connection
   */
  public Connection getCurrentConnection() {
    return connectionTL.get();
  }

  /**
   * 设置当前连接对象
   *
   * @param connection connection
   */
  public void setCurrentConnection(Connection connection) {
    connectionTL.set(connection);
  }

  /**
   * 移除连接对象
   */
  public void rmCurrentConnection() {
    connectionTL.remove();
  }

  /**
   * 事务管理
   *
   * @return
   */
  public TransactionManager getCurrentTransactionManager() {
    return transactionManagerTL.get();
  }

  /**
   * 设置事务对象
   *
   * @param transactionManager
   */
  public void setCurrentTransactionManager(TransactionManager transactionManager) {
    transactionManagerTL.set(transactionManager);
  }

  public void initCurrentTransactionManager(boolean readonly, int level) {
    setCurrentTransactionManager(new TransactionManager(this, readonly, level));
  }

  /**
   * 移除事务对象
   */
  public void rmCurrentTransactionManager() {
    transactionManagerTL.remove();
  }

  /**
   * 开始事务
   *
   * @throws TransactionException
   */
  public void beginTransaction() throws TransactionException {
    TransactionManager transactionManager = getCurrentTransactionManager();
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
    TransactionManager transactionManager = getCurrentTransactionManager();
    if (transactionManager != null) {
      transactionManager.commit();
    }
  }

  /**
   * 回滚事务
   *
   * @throws TransactionException
   */
  public void rollbackTransaction() {
    TransactionManager transactionManager = getCurrentTransactionManager();
    if (transactionManager != null) {
      transactionManager.rollback();
    }
  }

  /**
   * 结束事务
   *
   * @throws TransactionException
   */
  public void endTranasaction() {
    TransactionManager transactionManager = getCurrentTransactionManager();
    if (transactionManager != null) {
      transactionManager.end();
    }
    rmCurrentTransactionManager();
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
