package cn.dreampie.route.interceptor.transaction;

import cn.dreampie.orm.DataSourceMeta;
import cn.dreampie.orm.Metadatas;
import cn.dreampie.orm.exception.ActiveRecordException;
import cn.dreampie.route.interceptor.Interceptor;
import cn.dreampie.route.invocation.Invocation;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by wangrenhui on 15/1/3.
 */
public class DsTransactionExcutor {
  private String dsName;
  private int level;


  public DsTransactionExcutor(String dsName, int level) {
    this.dsName = dsName;
    this.level = level;
  }


  public void transaction(Interceptor interceptor, Invocation ri) {
    DataSourceMeta dataSourceMeta = Metadatas.getDataSourceMeta(dsName);
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
      interceptor.intercept(ri);
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
