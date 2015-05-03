package cn.dreampie.orm.dialect;

/**
 * Created by ice on 15-1-12.
 */
public class H2Dialect extends PostgreSQLDialect {

  public String getDbType() {
    return "h2";
  }

  public String driverClass() {
    return "org.h2.Driver";
  }
}
