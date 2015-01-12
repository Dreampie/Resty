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

  public String select(String table, String... columns) {
    StringBuilder query = new StringBuilder().append("SELECT ");
    query.append(Joiner.on(", ").join(columns));
    query.append(" FROM ");
    query.append(table);
    return query.toString();
  }


  public String select(String table, String where) {
    return where != null ? "SELECT * FROM " + table + " WHERE " + where : select(table);
  }

  public String select(String table, String where, String... columns) {
    if (where == null) return select(table, columns);
    if (columns == null || columns.length <= 0) return select(table, where);
    StringBuilder query = new StringBuilder().append("SELECT ");
    query.append(Joiner.on(", ").join(columns));
    query.append(" FROM " + table + " WHERE " + where);
    return query.toString();
  }

  protected void appendQuestions(StringBuilder query, int count) {
    for (int i = 0; i < count; i++) {
      if (i == 0)
        query.append("?");
      else
        query.append(",?");
    }
  }

  public String insert(String table, String... columns) {
    StringBuilder query = new StringBuilder().append("INSERT INTO ").append(table).append(" (");
    query.append(Joiner.on(", ").join(columns));
    query.append(") VALUES (");
    appendQuestions(query, columns.length);
    query.append(')');
    return query.toString();
  }


  public String delete(String table) {
    return "DELETE FROM " + table;
  }


  public String delete(String table, String where) {
    return "DELETE FROM " + table + " WHERE " + where;
  }


  public String update(String table, String... columns) {
    StringBuilder query = new StringBuilder().append("UPDATE ").append(table).append(" SET ");
    query.append(Joiner.on("=?, ").join(columns));
    return query.toString();
  }

  public String update(String table, String where, String... columns) {
    if (where == null) return update(table, columns);
    StringBuilder query = new StringBuilder().append("UPDATE ").append(table).append(" SET ");
    query.append(Joiner.on("=?, ").join(columns));
    query.append(" WHERE " + where);
    return query.toString();
  }


  public String count(String table) {
    return "SELECT COUNT(*) FROM " + table;
  }


  public String count(String table, String where) {
    return "SELECT COUNT(*) FROM " + table + " WHERE " + where;
  }

  public String countWith(String sql) {
    return "SELECT COUNT(*) FROM (" + sql + ") count_alias";
  }


  public String paginate(int pageNo, int pageSize, String table) {
    return paginateWith(pageNo, pageSize, select(table));
  }

  public String paginate(int pageNo, int pageSize, String table, String... columns) {
    return paginateWith(pageNo, pageSize, select(table, columns));
  }

  public String paginate(int pageNo, int pageSize, String table, String where) {
    return paginateWith(pageNo, pageSize, select(table, where));
  }

  public String paginate(int pageNo, int pageSize, String table, String where, String... columns) {
    return paginateWith(pageNo, pageSize, select(table, where, columns));
  }
}
