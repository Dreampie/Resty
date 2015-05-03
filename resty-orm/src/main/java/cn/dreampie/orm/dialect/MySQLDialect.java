package cn.dreampie.orm.dialect;

/**
 * MySQLDialect
 */
public class MySQLDialect extends PostgreSQLDialect {

  public String getDbType() {
    return "mysql";
  }

  public String validQuery() {
    return "SELECT 1";
  }

  public String driverClass() {
    return "com.mysql.jdbc.Driver";
  }
}
