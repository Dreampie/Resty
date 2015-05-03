package cn.dreampie.orm.dialect;

/**
 * Created by ice on 15-1-12.
 */
public class PostgreSQLDialect extends DefaultDialect {

  public String getDbType() {
    return "postgreSQL";
  }

  public String validQuery() {
    return "SELECT VERSION();";
  }

  public String driverClass() {
    return "org.postgresql.Driver";
  }

  public String paginateWith(int pageNumber, int pageSize, String sql) {
    int offset = pageSize * (pageNumber - 1);
    return sql + " LIMIT " + pageSize + " OFFSET " + offset;
  }
}
