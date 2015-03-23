package cn.dreampie.orm.transaction;

import cn.dreampie.log.Logger;
import cn.dreampie.orm.DataSourceMeta;
import cn.dreampie.orm.Metadatas;
import cn.dreampie.orm.exception.TransactionException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by wangrenhui on 15/1/3.
 */
public class TransactionExecutor {

  private final static Logger logger = Logger.getLogger(TransactionExecutor.class);
  private String dsName;
  private int level;


  public TransactionExecutor(String dsName, int level) {
    this.dsName = dsName;
    this.level = level;
  }


  public void transaction(TransactionAspect aspect, InvocationHandler ih, Object proxy, Method method, Object[] args) {
    DataSourceMeta dataSourceMeta = Metadatas.getDataSourceMeta(dsName);
    Connection conn = dataSourceMeta.getCurrentConnection();
    if (conn != null) {
      try {
        if (conn.getTransactionIsolation() < level) {
          conn.setTransactionIsolation(level);
        }
        return;
      } catch (SQLException e) {
        throw new TransactionException(e.getMessage(), e);
      }
    }

    Boolean autoCommit = null;
    try {
      conn = dataSourceMeta.getConnection();
      autoCommit = conn.getAutoCommit();
      dataSourceMeta.setCurrentConnection(conn);
      conn.setTransactionIsolation(level);  // conn.setTransactionIsolation(transactionLevel);
      conn.setAutoCommit(false);
      aspect.aspect(ih, proxy, method, args);
      conn.commit();
    } catch (Throwable t) {
      if (conn != null) try {
        conn.rollback();
      } catch (SQLException e) {
        logger.error("Could not rollback " + dsName + " connection.", e);
      }
      throw new TransactionException(t.getMessage(), t.getCause());
    } finally {
      try {
        if (conn != null) {
          if (autoCommit != null)
            conn.setAutoCommit(autoCommit);
          conn.close();
        }
      } catch (Throwable t) {
        logger.error("Could not close " + dsName + " connection.", t);  // can not throw exception here, otherwise the more important exception in previous catch block can not be thrown
      } finally {
        dataSourceMeta.rmCurrentConnection();  // prevent memory leak
      }
    }
  }
}
