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
  private int level;


  public TransactionExecutor(String dsName, int level) {
    this.dsName = dsName;
    this.level = level;
  }


  public void transaction(TransactionAspect aspect, InvocationHandler ih, Object proxy, Method method, Object[] args) {
    TransactionManager transactionManager = new TransactionManager(Metadata.getDataSourceMeta(dsName));
    try {
      transactionManager.begin(level);

      aspect.aspect(ih, proxy, method, args);
      
      transactionManager.commit();
    } catch (Throwable t) {
      transactionManager.rollback();
      throw new TransactionException(t.getMessage(), t.getCause());
    } finally {
      transactionManager.end();
    }
  }
}
