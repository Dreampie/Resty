package cn.dreampie.orm.dialect;


import cn.dreampie.common.util.Joiner;


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

  public String select(String table) {
    return "SELECT * FROM " + table;
  }

  protected String getAlias(String alias) {
    if (null != alias && !"".equals(alias.trim())) {
      alias = " " + alias;
    } else {
      alias = "";
    }
    return alias;
  }

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


  public String select(String table, String... columns) {
    if (columns == null || columns.length <= 0) return select(table);
    return "SELECT " + Joiner.on(", ").join(columns) + " FROM " + table;
  }


  public String select(String table, String alias, String where) {
    return where != null ? "SELECT * FROM " + table + getAlias(alias) + " WHERE " + where : select(table, alias);
  }

  public String select(String table, String alias, String where, String... columns) {
    if (where == null) return select(table, columns);
    if (columns == null || columns.length <= 0) return select(table, alias, where);
    return "SELECT " + Joiner.on(", ").join(getPrefix(alias, columns)) + " FROM " + table + getAlias(alias) + " WHERE " + where;
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
    StringBuilder sql = new StringBuilder().append("INSERT INTO ").append(table).append(" (");
    sql.append(Joiner.on(", ").join(columns));
    sql.append(") VALUES (");
    appendQuestions(sql, columns.length);
    sql.append(')');
    return sql.toString();
  }


  public String delete(String table) {
    return "DELETE FROM " + table;
  }


  public String delete(String table, String where) {
    return "DELETE FROM " + table + " WHERE " + where;
  }


  public String update(String table, String... columns) {
    return "UPDATE " + table + " SET " + Joiner.on("=?, ").join(columns) + "=?";
  }

  public String update(String table, String alias, String where, String... columns) {
    if (where == null) return update(table, columns);
    return "UPDATE " + table + getAlias(alias) + " SET " + Joiner.on("=?, ").join(getPrefix(alias, columns)) + "=? WHERE " + where;
  }


  public String count(String table) {
    return "SELECT COUNT(*) FROM " + table;
  }


  public String count(String table, String alias, String where) {
    return "SELECT COUNT(*) FROM " + table + getAlias(alias) + " WHERE " + where;
  }

  public String countWith(String sql) {
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

  public static void main(String[] args) {
    MySQLDialect mySQLDialect = new MySQLDialect();
    mySQLDialect.getAlias("");
    mySQLDialect.getPrefix("", "*");
    System.out.println(Joiner.on(", ").join(new String[]{"*"}));
  }
}
