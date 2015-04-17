package cn.dreampie.orm.transaction;

import cn.dreampie.log.Logger;
import cn.dreampie.orm.DataSourceMeta;
import cn.dreampie.orm.exception.TransactionException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by wangrenhui on 15/4/3.
 */
public class TransactionManager {
  private final static Logger logger = Logger.getLogger(TransactionExecutor.class);
  private DataSourceMeta dataSourceMeta;
  private Boolean isReadonly;
  private Boolean autoCommit;

  public TransactionManager(DataSourceMeta dataSourceMeta) {
    this.dataSourceMeta = dataSourceMeta;
  }

  /**
   * 开始事务
   */
  public void begin() {
    begin(false, Connection.TRANSACTION_READ_COMMITTED);
  }

  /**
   * 开始事务
   *
   * @param level 事务级别
   */
  public void begin(boolean readonly, int level) throws TransactionException {
    Connection conn = dataSourceMeta.getCurrentConnection();
    try {
      if (conn == null) {
        conn = dataSourceMeta.getConnection();
        dataSourceMeta.setCurrentConnection(conn);
      }
      if (!readonly) {
        autoCommit = conn.getAutoCommit();
        if (conn.getAutoCommit()) {
          conn.setAutoCommit(false);
        }
      } else {
        isReadonly = conn.isReadOnly();
        conn.setReadOnly(true);
      }
      conn.setTransactionIsolation(level);
    } catch (SQLException e) {
      throw new TransactionException(e.getMessage(), e);
    }
  }

  /**
   * 提交事务
   */
  public void commit() throws TransactionException {
    Connection conn = dataSourceMeta.getCurrentConnection();
    try {
      if (conn != null) {
        if (isReadonly == null || !isReadonly) {
          if (!conn.getAutoCommit()) {
            conn.commit();
          }
        }
      }
    } catch (SQLException e) {
      throw new TransactionException(e.getMessage(), e);
    }
  }

  /**
   * 设置Connection的原始状态
   */
  public void end() {
    Connection conn = dataSourceMeta.getCurrentConnection();
    try {
      if (conn != null) {
        if (autoCommit != null) {
          conn.setAutoCommit(autoCommit);
          autoCommit = null;
        }
        if (isReadonly != null) {
          conn.setReadOnly(isReadonly);
          isReadonly = null;
        }
        dataSourceMeta.rmCurrentConnection();
        dataSourceMeta.close(conn);
      }
    } catch (SQLException e) {
      logger.error("Could not end " + dataSourceMeta.getDsName() + " connection.", e);
    }
  }

  /**
   * 发生异常回滚事务
   */
  public void rollback() {
    Connection conn = dataSourceMeta.getCurrentConnection();
    try {
      if (conn != null) {
        if (isReadonly == null || !isReadonly) {
          conn.rollback();
        }
      }
    } catch (SQLException e) {
      logger.error("Could not rollback " + dataSourceMeta.getDsName() + " connection.", e);
    }
  }
}
