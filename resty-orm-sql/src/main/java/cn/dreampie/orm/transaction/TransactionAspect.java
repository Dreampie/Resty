package cn.dreampie.orm.transaction;

import cn.dreampie.orm.DataSourceMeta;
import cn.dreampie.orm.Metadatas;
import cn.dreampie.orm.exception.ActiveRecordException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by wangrenhui on 15/1/2.
 */
public class TransactionAspect implements Aspect {

  public void aspect(InvocationHandler ih, Object proxy, Method method, Object[] args) {
    Transaction transactionAnn = method.getAnnotation(Transaction.class);
    if (transactionAnn != null) {
      String name = transactionAnn.name();
      int level = transactionAnn.level();
      transaction(ih, name, level, proxy, method, args);
    }

  }

  public void transaction(InvocationHandler ih, String name, int level, Object proxy, Method method, Object[] args) {
    DataSourceMeta dataSourceMeta = Metadatas.getDataSourceMeta(name);
    Connection conn = dataSourceMeta.getCurrentConnection();
    if (conn != null) {
      try {
        if (conn.getTransactionIsolation() < level) {
          conn.setTransactionIsolation(level);
        }
        return;
      } catch (SQLException e) {
        throw new ActiveRecordException(e);
      }
    }

    Boolean autoCommit = null;
    try {
      conn = dataSourceMeta.getConnection();
      autoCommit = conn.getAutoCommit();
      dataSourceMeta.setCurrentConnection(conn);
      conn.setTransactionIsolation(level);  // conn.setTransactionIsolation(transactionLevel);
      conn.setAutoCommit(false);
      ih.invoke(proxy, method, args);
      conn.commit();
    } catch (Throwable t) {
      if (conn != null) try {
        conn.rollback();
      } catch (Exception e) {
        e.printStackTrace();
      }
      throw new ActiveRecordException(t);
    } finally {
      try {
        if (conn != null) {
          if (autoCommit != null)
            conn.setAutoCommit(autoCommit);
          conn.close();
        }
      } catch (Throwable t) {
        t.printStackTrace();  // can not throw exception here, otherwise the more important exception in previous catch block can not be thrown
      } finally {
        dataSourceMeta.rmCurrentConnection();  // prevent memory leak
      }
    }
  }
}
