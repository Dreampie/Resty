package cn.dreampie.orm.transaction;

import cn.dreampie.log.Logger;
import cn.dreampie.orm.Metadata;
import cn.dreampie.orm.exception.TransactionException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangrenhui on 15/1/2.
 */
public class TransactionAspect implements Aspect {

  private static final Logger logger = Logger.getLogger(AspectHandler.class);

  public Object aspect(InvocationHandler ih, Object proxy, Method method, Object[] args) throws Throwable {
    Object result = null;

    List<TransactionManager> transactionManagers = null;
    Transaction transactionAnn = method.getAnnotation(Transaction.class);
    if (transactionAnn != null) {
      String[] names = transactionAnn.name();
      if (names.length == 0) {
        names = new String[]{Metadata.getDefaultDsName()};
      }
      int[] levels = transactionAnn.level();
      boolean[] readonlys = transactionAnn.readonly();
      transactionManagers = new ArrayList<TransactionManager>();
      TransactionManager transactionManager;
      try {
        for (int i = 0; i < names.length; i++) {
          transactionManager = new TransactionManager(Metadata.getDataSourceMeta(names[i]));
          transactionManagers.add(transactionManager);
          transactionManager.begin(readonlys.length == 1 ? readonlys[0] : readonlys[i], levels.length == 1 ? levels[0] : levels[i]);
        }
        //执行操作
        result = ih.invoke(proxy, method, args);
        for (TransactionManager tm : transactionManagers) {
          tm.commit();
        }
      } catch (Throwable t) {
        for (TransactionManager tm : transactionManagers) {
          tm.rollback();
        }
        Throwable cause = t.getCause();
        if (cause != null) {
          throw new TransactionException(cause.getMessage(), cause);
        } else {
          throw new TransactionException(t.getMessage(), t);
        }
      } finally {
        for (TransactionManager tm : transactionManagers) {
          tm.end();
        }
      }
    } else {
      //执行操作
      result = ih.invoke(proxy, method, args);
    }
    return result;
  }

}
