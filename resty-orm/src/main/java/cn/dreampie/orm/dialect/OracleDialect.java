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

  public String driverClass() {
    return "oracle.jdbc.driver.OracleDriver";
  }

  public String paginateWith(int pageNumber, int pageSize, String sql) {
    if (pageNumber == 1 && pageSize == 1) {
      //如果sql本身只返回一个结果
      if (selectSinglePattern.matcher(sql).find()) {
        return sql;
      }
    }
    int start = (pageNumber - 1) * pageSize + 1;
    int end = pageNumber * pageSize;
    return "SELECT * FROM ( SELECT row_.*, ROWNUM rownum_ FROM (  " + sql + " ) row_ WHERE ROWNUM <= " + end + ") paginate_alias" + " WHERE paginate_alias.rownum_ >= " + start;
  }

}
