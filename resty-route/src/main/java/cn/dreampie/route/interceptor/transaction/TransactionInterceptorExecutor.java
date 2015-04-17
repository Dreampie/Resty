package cn.dreampie.route.interceptor.transaction;

import cn.dreampie.orm.Metadata;
import cn.dreampie.orm.exception.TransactionException;
import cn.dreampie.orm.transaction.TransactionManager;
import cn.dreampie.route.core.RouteInvocation;
import cn.dreampie.route.interceptor.Interceptor;

/**
 * Created by wangrenhui on 15/1/3.
 */
public class TransactionInterceptorExecutor {

  private String dsName;
  private boolean readonly;
  private int level;


  public TransactionInterceptorExecutor(String dsName, boolean readonly, int level) {
    this.dsName = dsName;
    this.readonly = readonly;
    this.level = level;
  }


  public void transaction(Interceptor interceptor, RouteInvocation ri) {
    TransactionManager transactionManager = new TransactionManager(Metadata.getDataSourceMeta(dsName));
    try {
      transactionManager.begin(readonly, level);
      //执行过滤
      interceptor.intercept(ri);

      transactionManager.commit();
    } catch (Throwable t) {
      transactionManager.rollback();
      throw new TransactionException(t.getMessage(), t);
    } finally {
      transactionManager.end();
    }
  }
}
