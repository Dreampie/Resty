package cn.dreampie.orm.dialect;

import cn.dreampie.orm.exception.DBException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangrenhui on 14/12/31.
 */
public class DialectFactory {
  private static Map<String, Dialect> dialectMap = new HashMap<String, Dialect>() {{
    put("h2", new H2Dialect());
    put("mssql", new MSSQLDialect());
    put("mysql", new MySQLDialect());
    put("oracle", new OracleDialect());
    put("postgreSQL", new PostgreSQLDialect());
    put("sqlite", new SQLiteDialect());
  }};

  public static Map<String, Dialect> getDialectMap() {
    return dialectMap;
  }

  public static void setDialectMap(Map<String, Dialect> dialectMap) {
    DialectFactory.dialectMap = dialectMap;
  }

  public static Dialect get(String dialectName) {
    Dialect dialect = dialectMap.get(dialectName);
    if (dialect == null) {
      throw new DBException("Dialect could not found for name " + dialectName + ".only support like this:" + dialectMap.keySet().toString());
    }
    return dialect;
  }

  public static void addDialectMap(Map<String, Dialect> dialectMap) {
    DialectFactory.dialectMap.putAll(dialectMap);
  }

  public static void addDialect(String dialectName, Dialect dialect) {
    DialectFactory.dialectMap.put(dialectName, dialect);
  }
}
