package cn.dreampie.route.interceptor.transaction;

import cn.dreampie.orm.Metadata;
import cn.dreampie.orm.exception.TransactionException;
import cn.dreampie.orm.transaction.Transaction;
import cn.dreampie.orm.transaction.TransactionManager;
import cn.dreampie.route.core.RouteInvocation;
import cn.dreampie.route.interceptor.Interceptor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangrenhui on 15/1/2.
 */
public class TransactionInterceptor implements Interceptor {


  public void intercept(RouteInvocation ri) {

    List<TransactionManager> transactionManagers = null;
    Transaction transactionAnn = ri.getMethod().getAnnotation(Transaction.class);
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
        ri.invoke();
        for (TransactionManager tm : transactionManagers) {
          tm.commit();
        }
      } catch (Throwable t) {
        for (TransactionManager tm : transactionManagers) {
          tm.rollback();
        }
        throw new TransactionException(t.getMessage(), t);
      } finally {
        for (TransactionManager tm : transactionManagers) {
          tm.end();
        }
      }
    } else {
      //执行操作
      ri.invoke();
    }
  }

}
