package cn.dreampie.orm;


import cn.dreampie.log.Logger;
import cn.dreampie.orm.exception.DBException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;

/**
 * SqlPrinter.
 */
public class SqlPrinter implements InvocationHandler {

  private Connection conn;
  private static final Logger logger = Logger.getLogger(SqlPrinter.class);

  SqlPrinter(Connection conn) {
    this.conn = conn;
  }

  Connection getConnection() {
    Class clazz = conn.getClass();
    return (Connection) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{Connection.class}, this);
  }

  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      String methodName = method.getName();
      if (methodName.equals("prepareStatement") || methodName.equals("prepareCall")) {
        String info = "Sql: " + args[0];
        if (logger.isInfoEnabled())
          logger.info(info);
      }
      return method.invoke(conn, args);
    } catch (InvocationTargetException e) {
      throw new DBException(e.getMessage(), e);
    }
  }
}




