package cn.dreampie.orm.dialect;

import cn.dreampie.orm.exception.DBException;

import java.util.regex.Matcher;

/**
 * Created by ice on 15-1-12.
 */
public class MSSQLDialect extends DefaultDialect {

  public String getDbType() {
    return "mssql";
  }

  public String validQuery() {
    return "select 1";
  }

  public String driverClass() {
    return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
  }

  public String paginateWith(int pageNumber, int pageSize, String sql) {
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
      throw new DBException("MSSQL offset queries require an order by column.");
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
