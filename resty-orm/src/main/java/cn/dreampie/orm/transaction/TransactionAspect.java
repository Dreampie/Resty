package cn.dreampie.orm.transaction;

import cn.dreampie.log.Logger;
import cn.dreampie.orm.DataSourceMeta;
import cn.dreampie.orm.Metadata;
import cn.dreampie.orm.aspect.Aspect;
import cn.dreampie.orm.aspect.AspectHandler;
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

    List<DataSourceMeta> dataSourceMetas = null;
    Transaction transactionAnn = method.getAnnotation(Transaction.class);
    if (transactionAnn != null) {
      String[] names = transactionAnn.name();
      if (names.length == 0) {
        names = new String[]{Metadata.getDefaultDsmName()};
      }
      int[] levels = transactionAnn.level();
      boolean[] readonlys = transactionAnn.readonly();
      dataSourceMetas = new ArrayList<DataSourceMeta>();
      DataSourceMeta dataSourceMeta;
      try {
        for (int i = 0; i < names.length; i++) {
          dataSourceMeta = Metadata.getDataSourceMeta(names[i]);
          dataSourceMeta.initTransaction(readonlys.length == 1 ? readonlys[0] : readonlys[i], levels.length == 1 ? levels[0] : levels[i]);
          dataSourceMetas.add(dataSourceMeta);
        }
        //执行操作
        result = ih.invoke(proxy, method, args);
        for (DataSourceMeta dsm : dataSourceMetas) {
          dsm.commitTransaction();
        }
      } catch (Throwable t) {
        for (DataSourceMeta dsm : dataSourceMetas) {
          dsm.rollbackTransaction();
        }
        String message = t.getMessage();
        Throwable cause = t.getCause();
        if (message == null) {
          if (cause != null) {
            message = cause.getMessage();
          }
        }
        throw new TransactionException(message, t);
      } finally {
        for (DataSourceMeta dsm : dataSourceMetas) {
          dsm.endTranasaction();
        }
      }
    } else {
      //执行操作
      result = ih.invoke(proxy, method, args);
    }
    return result;
  }

}
