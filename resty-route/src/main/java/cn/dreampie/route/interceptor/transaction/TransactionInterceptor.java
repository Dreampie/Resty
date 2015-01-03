package cn.dreampie.route.interceptor.transaction;

import cn.dreampie.orm.transaction.Transaction;
import cn.dreampie.route.core.RouteInvocation;
import cn.dreampie.route.interceptor.Interceptor;
import cn.dreampie.route.invocation.Invocation;

/**
 * Created by wangrenhui on 15/1/2.
 */
public class TransactionInterceptor implements Interceptor {

  private DsTransactionExcutor[] excutors;
  private int index = 0;

  public void intercept(Invocation ri) {
    if (index == 0) {
      Transaction transactionAnn = ((RouteInvocation) ri).getMethod().getAnnotation(Transaction.class);
      if (transactionAnn != null) {
        String[] names = transactionAnn.name();
        int[] levels = transactionAnn.level();
        excutors = new DsTransactionExcutor[names.length];
        for (int i = 0; i < names.length; i++) {
          excutors[i] = new DsTransactionExcutor(names[i], levels.length == 1 ? levels[0] : levels[i]);
        }
      }
    }

    if (excutors != null && excutors.length > 0) {
      if (excutors.length > 0) {
        if (index < excutors.length)
          excutors[index++].transaction(this, ri);
        else if (index++ == excutors.length) {
          index = 0;
          excutors = null;
          ri.invoke();
          return;
        }
      }
    }
  }

}
