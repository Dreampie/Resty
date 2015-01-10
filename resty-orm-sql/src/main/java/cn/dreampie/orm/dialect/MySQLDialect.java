package cn.dreampie.orm.dialect;

/**
 * MySQLDialect
 */
public class MySQLDialect extends DefaultDialect {

  public String getDbType() {
    return "mysql";
  }

  public String validQuery() {
    return "SELECT 1";
  }

  public String paginateWith(int pageNo, int pageSize, String sql) {
    int offset = pageSize * (pageNo - 1);
    return sql + " limit " + offset + ", " + pageSize;
  }
}
