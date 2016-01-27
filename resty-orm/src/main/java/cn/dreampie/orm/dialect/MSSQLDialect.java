package cn.dreampie.orm.dialect;

import java.util.regex.Matcher;

/**
 * Created by ice on 15-1-12.
 */
public class MSSQLDialect extends DefaultDialect {

  public String getDbType() {
    return "mssql";
  }

  public String validQuery() {
    return "SELECT 1";
  }

  public String driverClass() {
    return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
  }

  public String paginateWith(int pageNumber, int pageSize, String sql) {
    if (pageNumber == 1 && pageSize == 1) {
      //如果sql本身只返回一个结果
      if (selectSinglePattern.matcher(sql).find()) {
        return sql;
      }
    }

    int offset = pageSize * (pageNumber - 1);

    String orderBys = null;
    Matcher om = orderPattern.matcher(sql);
    if (om.find()) {
      int oindex = om.end();
      if (oindex > sql.lastIndexOf(")")) {
        orderBys = sql.substring(oindex, sql.length());
        sql = sql.substring(0, om.start());
      }
    }

    //mssql ROW_NUMBER分页必须要至少一个ORDER BY
    if (orderBys == null) {
      orderBys = "CURRENT_TIMESTAMP";
      //throw new DBException("MSSQL offset queries require an order by column.");
    }

    StringBuilder querySql = new StringBuilder();
    querySql.append("SELECT paginate_alias.* FROM (SELECT ROW_NUMBER() OVER (ORDER BY ");
    querySql.append(orderBys);
    querySql.append(") rownumber,");

    Matcher sm = selectPattern.matcher(sql);
    if (sm.find()) {
      querySql.append(' ').append(sql.substring(sm.end()));
    } else {
      querySql.append(sql);
    }

    // T-SQL offset starts with 1, not like MySQL with 0;
    querySql.append(") paginate_alias WHERE rownumber BETWEEN ").append(offset + 1)
        .append(" AND ").append(pageSize + offset);

    return querySql.toString();
  }

}
