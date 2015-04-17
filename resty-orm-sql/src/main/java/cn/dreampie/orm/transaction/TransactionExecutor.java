package cn.dreampie.orm.transaction;

import cn.dreampie.orm.Metadata;
import cn.dreampie.orm.exception.TransactionException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by wangrenhui on 15/1/3.
 */
public class TransactionExecutor {

  private String dsName;
  private boolean readonly;
  private int level;


  public TransactionExecutor(String dsName, boolean readonly, int level) {
    this.dsName = dsName;
    this.readonly = readonly;
    this.level = level;
  }


  public Object transaction(TransactionAspect aspect, InvocationHandler ih, Object proxy, Method method, Object[] args) {
    TransactionManager transactionManager = new TransactionManager(Metadata.getDataSourceMeta(dsName));
    Object result = null;
    try {
      transactionManager.begin(readonly, level);

      result = aspect.aspect(ih, proxy, method, args);

      transactionManager.commit();
    } catch (Throwable t) {
      transactionManager.rollback();
      throw new TransactionException(t.getMessage(), t);
    } finally {
      transactionManager.end();
    }
    return result;
  }
}
