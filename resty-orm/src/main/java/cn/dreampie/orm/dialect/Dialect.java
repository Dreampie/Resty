package cn.dreampie.orm.dialect;

/**
 * @author wangrenhui
 */
public interface Dialect {
  public Class getColumnType(int type);

  public String getDbType();

  public String validQuery();

  public String driverClass();

  public String select(String table);

  public String select(String table, String... columns);

  public String select(String table, String alias, String where);

  public String select(String table, String alias, String where, String... columns);

  public String insert(String table, String... columns);

  public String insert(String table, String id, String sequence, String... columns);

  public String delete(String table);

  public String delete(String table, String where);

  public String update(String table, String... columns);

  public String update(String table, String alias, String where, String... columns);

  public String count(String table);

  public String count(String table, String alias, String where);

  public String countWith(String sql);

  public String paginate(int pageNumber, int pageSize, String table);

  public String paginate(int pageNumber, int pageSize, String table, String... columns);

  public String paginate(int pageNumber, int pageSize, String table, String alias, String where);

  public String paginate(int pageNumber, int pageSize, String table, String alias, String where, String... columns);

  public String paginateWith(int pageNumber, int pageSize, String sql);
}
