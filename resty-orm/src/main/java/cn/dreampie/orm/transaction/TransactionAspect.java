package cn.dreampie.orm.transaction;

import cn.dreampie.common.http.exception.WebException;
import cn.dreampie.log.Logger;
import cn.dreampie.orm.aspect.Aspect;
import cn.dreampie.orm.aspect.AspectHandler;
import cn.dreampie.orm.exception.TransactionException;
import cn.dreampie.orm.meta.DataSourceMeta;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by wangrenhui on 15/1/2.
 */
public class TransactionAspect implements Aspect {

  private static final Logger logger = Logger.getLogger(AspectHandler.class);

  public Object aspect(InvocationHandler ih, Object proxy, Method method, Object[] args) throws Throwable {
    Object result = null;

    TransactionManager transactionManager;
    Transactional transactionalAnn = method.getAnnotation(Transactional.class);
    if (transactionalAnn != null) {
      transactionManager = DataSourceMeta.initTransactionManager(transactionalAnn.readonly(), transactionalAnn.level());

      try {
        //执行操作
        result = ih.invoke(proxy, method, args);

        for (DataSourceMeta dataSourceMeta : transactionManager.getDataSourceMetas()) {
          dataSourceMeta.commitTransaction();
        }

      } catch (Throwable t) {
        for (DataSourceMeta dataSourceMeta : transactionManager.getDataSourceMetas()) {
          dataSourceMeta.commitTransaction();
        }

        String message = t.getMessage();
        if (message == null) {
          Throwable cause = t.getCause();
          if (cause != null) {
            message = cause.getMessage();
          }
        }
        if (t instanceof WebException) {
          throw t;
        } else {
          throw new TransactionException(message, t);
        }
      } finally {
        for (DataSourceMeta dataSourceMeta : transactionManager.getDataSourceMetas()) {
          dataSourceMeta.endTranasaction();
        }
      }
    } else {
      //执行操作
      result = ih.invoke(proxy, method, args);
    }
    return result;
  }

}
