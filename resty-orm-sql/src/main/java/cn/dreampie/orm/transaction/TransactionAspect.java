package cn.dreampie.orm.transaction;

import cn.dreampie.log.Logger;
import cn.dreampie.orm.Metadata;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by wangrenhui on 15/1/2.
 */
public class TransactionAspect implements Aspect {

  private static final Logger logger = Logger.getLogger(AspectHandler.class);
  private TransactionExecutor[] excutors;
  private int index = -1;

  public TransactionAspect() {
  }

  public TransactionAspect(int index, TransactionExecutor[] excutors) {
    this.index = index;
    this.excutors = excutors;
  }

  public Object aspect(InvocationHandler ih, Object proxy, Method method, Object[] args) throws Throwable {
    if (index == -1) {
      logger.info("Instance an TransactionAspect to add transaction for method %s.", method.getName());
      return new TransactionAspect(0, excutors).aspect(ih, proxy, method, args);
    }
    Object result = null;
    if (excutors == null) {
      Transaction transactionAnn = method.getAnnotation(Transaction.class);
      if (transactionAnn != null) {
        String[] names = transactionAnn.name();
        if (names.length == 0) {
          names = new String[]{Metadata.getDefaultDsName()};
        }
        boolean[] readonly = transactionAnn.readonly();
        int[] levels = transactionAnn.level();
        excutors = new TransactionExecutor[names.length];
        for (int i = 0; i < names.length; i++) {
          excutors[i] = new TransactionExecutor(names[i], readonly.length == 1 ? readonly[0] : readonly[i], levels.length == 1 ? levels[0] : levels[i]);
        }
      }
    }

    if (excutors != null) {
      if (index < excutors.length) {
        result = excutors[index++].transaction(this, ih, proxy, method, args);
      } else if (index++ == excutors.length) {
        index = -1;
        excutors = null;
        result = ih.invoke(proxy, method, args);
      }
      return result;
    } else {
      return ih.invoke(proxy, method, args);
    }

  }

}
