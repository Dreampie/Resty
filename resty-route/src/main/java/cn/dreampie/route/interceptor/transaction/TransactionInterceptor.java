package cn.dreampie.route.interceptor.transaction;

import cn.dreampie.orm.Metadata;
import cn.dreampie.orm.transaction.Transaction;
import cn.dreampie.route.core.RouteInvocation;
import cn.dreampie.route.interceptor.Interceptor;

/**
 * Created by wangrenhui on 15/1/2.
 */
public class TransactionInterceptor implements Interceptor {

  private static final ThreadLocal<TransactionInterceptorExecutor[]> excutorsTL = new ThreadLocal<TransactionInterceptorExecutor[]>();
  private static final ThreadLocal<Integer> indexTL = new ThreadLocal<Integer>() {
    protected Integer initialValue() {
      return 0;
    }
  };

  public Object intercept(RouteInvocation ri) {
    Object result = null;
    int index = indexTL.get();
    TransactionInterceptorExecutor[] excutors = excutorsTL.get();
    if (excutors == null) {
      Transaction transactionAnn = ri.getMethod().getAnnotation(Transaction.class);
      if (transactionAnn != null) {
        String[] names = transactionAnn.name();
        if (names.length == 0) {
          names = new String[]{Metadata.getDefaultDsName()};
        }
        boolean[] readonly = transactionAnn.readonly();
        int[] levels = transactionAnn.level();
        excutors = new TransactionInterceptorExecutor[names.length];
        for (int i = 0; i < names.length; i++) {
          excutors[i] = new TransactionInterceptorExecutor(names[i], readonly.length == 1 ? readonly[0] : readonly[i], levels.length == 1 ? levels[0] : levels[i]);
        }
        excutorsTL.set(excutors);
      }
    }

    if (excutors != null) {
      if (index < excutors.length) {
        indexTL.set(index + 1);
        result = excutors[index].transaction(this, ri);
      } else if (index == excutors.length) {
        indexTL.set(index + 1);
        result = ri.invoke();
      }
      return result;
    } else {
      return ri.invoke();
    }
  }

}
