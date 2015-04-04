package cn.dreampie.orm.dialect;

/**
 * Created by ice on 15-1-12.
 */
public class SQLiteDialect extends MySQLDialect {
  public String getDbType() {
    return "sqlite";
  }

  public String driverClass() {
    return "org.sqlite.JDBC";
  }
}
