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

  public String paginateWith(int pageNo, int pageSize, String sql) {
    int offset = pageSize * (pageNo - 1);
    return sql + " LIMIT " + pageSize + " OFFSET " + offset;
  }
}
