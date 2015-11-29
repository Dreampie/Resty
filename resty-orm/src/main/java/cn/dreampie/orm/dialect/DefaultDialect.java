package cn.dreampie.orm.dialect;


import cn.dreampie.common.util.Joiner;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 数据库	validationQuery
 * Oracle	select 1 from dual
 * DB2	select 1 from sysibm.sysdummy1
 * mysql	select 1
 * microsoft sql	select 1
 * hsqldb	select 1 from INFORMATION_SCHEMA.SYSTEM_USERS
 * postgresql	select version();
 * ingres	select 1
 * derby	select 1
 * H2	select 1
 */
public abstract class DefaultDialect implements Dialect {


  private static Map<Integer, Class> typeMappings = new HashMap<Integer, Class>() {{
    put(Types.INTEGER, Integer.class);
    put(Types.SMALLINT, Integer.class);
    put(Types.BIGINT, Long.class);
    put(Types.FLOAT, Float.class);
    put(Types.DOUBLE, Double.class);
    put(Types.DECIMAL, BigDecimal.class);
  }};
  protected final Pattern selectPattern = Pattern.compile("^\\s*SELECT\\s+",
      Pattern.CASE_INSENSITIVE);
  protected final Pattern orderPattern = Pattern.compile("\\s+ORDER\\s+BY\\s+",
      Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
  protected final Pattern groupPattern = Pattern.compile("\\s+GROUP\\s+BY\\s+",
      Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
  protected final Pattern havingPattern = Pattern.compile("\\s+HAVING\\s+",
      Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

  protected final Pattern selectSinglePattern = Pattern.compile("^\\s*SELECT\\s+((COUNT)\\([\\s\\S]*\\)\\s*,?)+((\\s*)|(\\s+FROM[\\s\\S]*))?$",
      Pattern.CASE_INSENSITIVE);

  public Class getColumnType(int type) {
    return typeMappings.get(type);
  }

  /**
   * 获取别名
   *
   * @param alias 别名
   * @return String
   */
  protected String getAlias(String alias) {
    if (null != alias && !"".equals(alias.trim())) {
      alias = " " + alias;
    } else {
      alias = "";
    }
    return alias;
  }

  /**
   * 获取前缀
   *
   * @param alias   别名
   * @param columns 列
   * @return 列数组
   */
  protected String[] getPrefix(String alias, String... columns) {
    if (null != alias && !"".equals(alias.trim()) && columns.length > 0) {
      String[] newColumns = new String[columns.length];
      int i = 0;
      for (String column : columns) {
        if (column.contains(".")) {
          return columns;
        } else {
          newColumns[i] = alias + "." + column;
        }
        i++;
      }
      return newColumns;
    } else {
      return columns;
    }
  }

  /**
   * 拼接where条件
   *
   * @param where 条件
   * @return String
   */
  public String getWhere(String where) {
    if (!where.startsWith(" ")) {
      where = " " + where;
    }

    Matcher om = orderPattern.matcher(where);
    if (om.find()) {
      if (om.start() == 0) {
        return where;
      }
    }

    Matcher gm = groupPattern.matcher(where);
    if (gm.find()) {
      if (gm.start() == 0) {
        return where;
      }
    }

    Matcher hm = havingPattern.matcher(where);
    if (hm.find()) {
      if (hm.start() == 0) {
        return where;
      }
    }

    return " WHERE" + where;
  }

  public String select(String table) {
    return "SELECT * FROM " + table;
  }

  public String select(String table, String... columns) {
    if (columns == null || columns.length <= 0) return select(table);
    return "SELECT " + Joiner.on(", ").join(columns) + " FROM " + table;
  }


  public String select(String table, String alias, String where) {
    if (where == null || "".equals(where.trim())) return select(table);
    return "SELECT * FROM " + table + getAlias(alias) + getWhere(where);
  }

  public String select(String table, String alias, String where, String... columns) {
    if (where == null || "".equals(where.trim())) return select(table, columns);
    if (columns == null || columns.length <= 0) return select(table, alias, where);
    return "SELECT " + Joiner.on(", ").join(getPrefix(alias, columns)) + " FROM " + table + getAlias(alias) + getWhere(where);
  }

  protected void appendQuestions(StringBuilder sql, int count) {
    for (int i = 0; i < count; i++) {
      if (i == 0)
        sql.append("?");
      else
        sql.append(",?");
    }
  }

  public String insert(String table, String... columns) {
    return insert(table, null, null, columns);
  }

  public String insert(String table, String id, String sequence, String... columns) {
    StringBuilder sql = new StringBuilder().append("INSERT INTO ").append(table).append(" (");
    boolean inSequence = id != null && !id.isEmpty() && sequence != null && !sequence.isEmpty();

    if (inSequence) {
      sql.append(id).append(",");
    }
    sql.append(Joiner.on(", ").join(columns));
    sql.append(") VALUES (");
    if (inSequence) {
      sql.append(sequence).append(",");
    }
    appendQuestions(sql, columns.length);
    sql.append(')');
    return sql.toString();
  }

  public String delete(String table) {
    return "DELETE FROM " + table;
  }


  public String delete(String table, String where) {
    if (where == null || "".equals(where.trim())) return delete(table);
    return "DELETE FROM " + table + getWhere(where);
  }


  public String update(String table, String... columns) {
    if (columns == null || columns.length <= 0) throw new NullPointerException("Could not found columns to update.");
    return "UPDATE " + table + " SET " + Joiner.on("=?, ").join(columns) + "=?";
  }

  public String update(String table, String alias, String where, String... columns) {
    if (where == null || "".equals(where.trim())) return update(table, columns);
    if (columns == null || columns.length <= 0) throw new NullPointerException("Could not found columns to update.");
    return "UPDATE " + table + getAlias(alias) + " SET " + Joiner.on("=?, ").join(getPrefix(alias, columns)) + "=?" + getWhere(where);
  }

  public String count(String table) {
    return "SELECT COUNT(*) FROM " + table;
  }


  public String count(String table, String alias, String where) {
    if (where == null || "".equals(where.trim())) return count(table);
    return "SELECT COUNT(*) FROM " + table + getAlias(alias) + getWhere(where);
  }

  public String countWith(String sql) {
    Matcher om = orderPattern.matcher(sql);
    if (om.find()) {
      int index = om.end();
      if (index > sql.lastIndexOf(")")) {
        sql = sql.substring(0, om.start());
      }
    }
    return "SELECT COUNT(*) FROM (" + sql + ") count_alias";
  }

  public String paginate(int pageNumber, int pageSize, String table) {
    return paginateWith(pageNumber, pageSize, select(table));
  }

  public String paginate(int pageNumber, int pageSize, String table, String... columns) {
    return paginateWith(pageNumber, pageSize, select(table, columns));
  }

  public String paginate(int pageNumber, int pageSize, String table, String alias, String where) {
    return paginateWith(pageNumber, pageSize, select(table, alias, where));
  }

  public String paginate(int pageNumber, int pageSize, String table, String alias, String where, String... columns) {
    return paginateWith(pageNumber, pageSize, select(table, alias, where, columns));
  }

}
