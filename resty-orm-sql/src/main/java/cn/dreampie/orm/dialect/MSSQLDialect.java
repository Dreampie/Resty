package cn.dreampie.orm.dialect;

import cn.dreampie.common.util.Joiner;
import cn.dreampie.orm.exception.DBException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ice on 15-1-12.
 */
public class MSSQLDialect extends DefaultDialect {

  protected final Pattern selectPattern = Pattern.compile("^\\s*SELECT\\s*",
      Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

  protected final Pattern orderPattern = Pattern.compile("\\s*ORDER\\s*BY\\s*",
      Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

  public String getDbType() {
    return "mssql";
  }

  public String validQuery() {
    return "select 1";
  }

  public String paginateWith(int pageNo, int pageSize, String sql) {
    int offset = pageSize * (pageNo - 1);

    String orderBys = null;
    Matcher om = orderPattern.matcher(sql);
    if (om.find()) {
      int oindex = om.end();
      if (oindex > sql.toLowerCase().lastIndexOf(")")) {
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
    querySql.append(") AS rownumber,");

    Matcher sm = selectPattern.matcher(sql);
    if (sm.find()) {
      querySql.append(' ').append(sql.substring(sm.end()));
    } else {
      querySql.append(sql);
    }

    // T-SQL offset starts with 1, not like MySQL with 0;
    querySql.append(") AS paginate_alias WHERE rownumber BETWEEN ").append(offset + 1)
        .append(" AND ").append(pageSize + offset);

    return querySql.toString();
  }

}
