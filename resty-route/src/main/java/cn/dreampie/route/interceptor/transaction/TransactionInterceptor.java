package cn.dreampie.route.interceptor.transaction;

import cn.dreampie.common.http.exception.WebException;
import cn.dreampie.orm.DataSourceMeta;
import cn.dreampie.orm.Metadata;
import cn.dreampie.orm.transaction.Transaction;
import cn.dreampie.route.core.RouteInvocation;
import cn.dreampie.route.interceptor.Interceptor;
import cn.dreampie.route.interceptor.exception.InterceptorException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangrenhui on 15/1/2.
 */
public class TransactionInterceptor implements Interceptor {

  public void intercept(RouteInvocation ri) {

    List<DataSourceMeta> dataSourceMetas = null;
    Transaction transactionAnn = ri.getMethod().getAnnotation(Transaction.class);
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
        ri.invoke();
        for (DataSourceMeta dsm : dataSourceMetas) {
          dsm.commitTransaction();
        }
      } catch (Throwable t) {
        for (DataSourceMeta dsm : dataSourceMetas) {
          dsm.rollbackTransaction();
        }
        Throwable cause = t.getCause();

        if (cause == null) {
          cause = t;
        }
        if (cause instanceof WebException) {
          throw (WebException) cause;
        } else {
          throw new InterceptorException(cause.getMessage(), cause);
        }
      } finally {
        for (DataSourceMeta dsm : dataSourceMetas) {
          dsm.endTranasaction();
        }
      }
    } else {
      //执行操作
      ri.invoke();
    }
  }

}
