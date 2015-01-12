package cn.dreampie.orm.dialect;

/**
 * Created by ice on 15-1-12.
 */
public class OracleDialect extends DefaultDialect {
  public String getDbType() {
    return "oracle";
  }

  public String validQuery() {
    return "SELECT 1 FROM dual";
  }

  public String paginateWith(int pageNo, int pageSize, String sql) {
    int satrt = (pageNo - 1) * pageSize + 1;
    int end = pageNo * pageSize;
    return "SELECT * FROM ( SELECT row_.*, ROWNUM rownum_ FROM (  " + sql + " ) row_ where ROWNUM <= " + end + ") paginate_alias" + " WHERE paginate_alias.rownum_ >= " + satrt;
  }

}
