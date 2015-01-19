package cn.dreampie.orm;


import cn.dreampie.common.util.Joiner;
import cn.dreampie.log.Logger;
import cn.dreampie.orm.cache.QueryCache;
import cn.dreampie.orm.dialect.Dialect;
import cn.dreampie.orm.exception.DBException;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cn.dreampie.common.util.Checker.checkArgument;
import static cn.dreampie.common.util.Checker.checkNotNull;

/**
 * DS. Professional database query and update tool.
 */
public class DS {
  private static final Logger logger = Logger.getLogger(DS.class);
  public static final String DEFAULT_DS_NAME = "default";
  public static final String DEFAULT_PRIMARY_KAY = "id";
  public static final Object[] NULL_PARA_ARRAY = new Object[0];

  private DataSourceMeta dataSourceMeta;
  private boolean cached = false;

  public static DS use() {
    return DS.use(DEFAULT_DS_NAME, false);
  }

  public static DS use(boolean cached) {
    return DS.use(DEFAULT_DS_NAME, cached);
  }

  public static DS use(String dsName) {
    return DS.use(dsName, false);
  }

  public static DS use(String dsName, boolean cached) {
    DS ds = new DS();
    ds.dataSourceMeta = Metadatas.getDataSourceMeta(dsName);
    checkNotNull(ds.dataSourceMeta, "Could not found dsName " + dsName + ".");
    ds.cached = cached;
    return ds;
  }

  private PreparedStatement getPreparedStatement(String primaryKey, String sql, Object[] paras) throws SQLException {
    PreparedStatement pst = dataSourceMeta.getConnection().prepareStatement(sql, new String[]{primaryKey == null ? DEFAULT_PRIMARY_KAY : primaryKey});

    for (int i = 0; i < paras.length; i++) {
      pst.setObject(i + 1, paras[i]);
    }
    return pst;
  }


  private PreparedStatement getPreparedStatement(Connection connection, String primaryKey, String sql, Object[][] paras) throws SQLException {
    PreparedStatement pst = null;
    String key = primaryKey == null ? DEFAULT_PRIMARY_KAY : primaryKey;
    String[] returnKeys = new String[paras.length];
    for (int i = 0; i < paras.length; i++) {
      returnKeys[i] = key;
    }
    pst = connection.prepareStatement(sql, returnKeys);
    final int batchSize = 1000;
    int count = 0;
    for (int i = 0; i < paras.length; i++) {
      for (int j = 0; j < paras[i].length; j++) {
        pst.setObject(j + 1, paras[i][j]);
      }
      pst.addBatch();
      if (++count % batchSize == 0) {
        pst.executeBatch();
      }
    }
    return pst;
  }

  private Statement getPreparedStatement(Connection connection, List<String> sql) throws SQLException {
    Statement stmt = null;

    stmt = connection.createStatement();
    final int batchSize = 1000;
    int count = 0;
    int size = sql.size();
    for (int i = 0; i < size; i++) {
      stmt.addBatch(sql.get(i));
      if (++count % batchSize == 0) {
        stmt.executeBatch();
      }
    }
    return stmt;
  }


  public <T> List<T> query(String sql, Object... paras) {

    List result = new ArrayList();
    try {

      PreparedStatement pst = getPreparedStatement(DEFAULT_PRIMARY_KAY, sql, paras);
      ResultSet rs = pst.executeQuery();
      int colAmount = rs.getMetaData().getColumnCount();
      if (colAmount > 1) {
        while (rs.next()) {
          Object[] temp = new Object[colAmount];
          for (int i = 0; i < colAmount; i++) {
            temp[i] = rs.getObject(i + 1);
          }
          result.add(temp);
        }
      } else if (colAmount == 1) {
        while (rs.next()) {
          result.add(rs.getObject(1));
        }
      }
      dataSourceMeta.close(rs, pst);

    } catch (SQLException e) {
      throw new DBException(e);
    }
    return result;
  }

  /**
   * @param sql an SQL statement
   * @see #query(String, Object...)
   */
  public <T> List<T> query(String sql) {    // return  List<object[]> or List<object>
    return query(sql, NULL_PARA_ARRAY);
  }

  /**
   * Execute sql query and return the first result. I recommend add "limit 1" in your sql.
   *
   * @param sql   an SQL statement that may contain one or more '?' IN parameter placeholders
   * @param paras the parameters of sql
   * @return Object[] if your sql has select more than one column,
   * and it return Object if your sql has select only one column.
   */
  public <T> T queryFirst(String sql, Object... paras) {
    List<T> result = query(sql, paras);
    return (result.size() > 0 ? result.get(0) : null);
  }

  /**
   * @param sql an SQL statement
   * @see #queryFirst(String, Object...)
   */
  public <T> T queryFirst(String sql) {
    // return queryFirst(sql, NULL_PARA_ARRAY);
    List<T> result = query(sql, NULL_PARA_ARRAY);
    return (result.size() > 0 ? result.get(0) : null);
  }

  // 26 queryXxx method below -----------------------------------------------

  /**
   * Execute sql query just return one column.
   *
   * @param <T>   the type of the column that in your sql's select statement
   * @param sql   an SQL statement that may contain one or more '?' IN parameter placeholders
   * @param paras the parameters of sql
   * @return List<T>
   */
  public <T> T queryColumn(String sql, Object... paras) {
    List<T> result = query(sql, paras);
    if (result.size() > 0) {
      T temp = result.get(0);
      if (temp instanceof Object[])
        throw new DBException("Only one column can be queried.");
      return temp;
    }
    return null;
  }

  public <T> T queryColumn(String sql) {
    return (T) queryColumn(sql, NULL_PARA_ARRAY);
  }

  public String queryStr(String sql, Object... paras) {
    return (String) queryColumn(sql, paras);
  }

  public String queryStr(String sql) {
    return (String) queryColumn(sql, NULL_PARA_ARRAY);
  }

  public Integer queryInt(String sql, Object... paras) {
    return (Integer) queryColumn(sql, paras);
  }

  public Integer queryInt(String sql) {
    return (Integer) queryColumn(sql, NULL_PARA_ARRAY);
  }

  public Long queryLong(String sql, Object... paras) {
    return (Long) queryColumn(sql, paras);
  }

  public Long queryLong(String sql) {
    return (Long) queryColumn(sql, NULL_PARA_ARRAY);
  }

  public Double queryDouble(String sql, Object... paras) {
    return (Double) queryColumn(sql, paras);
  }

  public Double queryDouble(String sql) {
    return (Double) queryColumn(sql, NULL_PARA_ARRAY);
  }

  public Float queryFloat(String sql, Object... paras) {
    return (Float) queryColumn(sql, paras);
  }

  public Float queryFloat(String sql) {
    return (Float) queryColumn(sql, NULL_PARA_ARRAY);
  }

  public java.math.BigDecimal queryBigDecimal(String sql, Object... paras) {
    return (java.math.BigDecimal) queryColumn(sql, paras);
  }

  public java.math.BigDecimal queryBigDecimal(String sql) {
    return (java.math.BigDecimal) queryColumn(sql, NULL_PARA_ARRAY);
  }

  public byte[] queryBytes(String sql, Object... paras) {
    return (byte[]) queryColumn(sql, paras);
  }

  public byte[] queryBytes(String sql) {
    return (byte[]) queryColumn(sql, NULL_PARA_ARRAY);
  }

  public java.util.Date queryDate(String sql, Object... paras) {
    return (java.util.Date) queryColumn(sql, paras);
  }

  public java.util.Date queryDate(String sql) {
    return (java.util.Date) queryColumn(sql, NULL_PARA_ARRAY);
  }

  public java.sql.Time queryTime(String sql, Object... paras) {
    return (java.sql.Time) queryColumn(sql, paras);
  }

  public java.sql.Time queryTime(String sql) {
    return (java.sql.Time) queryColumn(sql, NULL_PARA_ARRAY);
  }

  public java.sql.Timestamp queryTimestamp(String sql, Object... paras) {
    return (java.sql.Timestamp) queryColumn(sql, paras);
  }

  public java.sql.Timestamp queryTimestamp(String sql) {
    return (java.sql.Timestamp) queryColumn(sql, NULL_PARA_ARRAY);
  }

  public Boolean queryBoolean(String sql, Object... paras) {
    return (Boolean) queryColumn(sql, paras);
  }

  public Boolean queryBoolean(String sql) {
    return (Boolean) queryColumn(sql, NULL_PARA_ARRAY);
  }

  public Number queryNumber(String sql, Object... paras) {
    return (Number) queryColumn(sql, paras);
  }

  public Number queryNumber(String sql) {
    return (Number) queryColumn(sql, NULL_PARA_ARRAY);
  }
  // 26 queryXxx method under -----------------------------------------------

  /**
   * Execute sql update
   */
  public int update(String sql, Object... paras) {
    int result = -1;
    //remove cache
    if (cached) {
      QueryCache.instance().purge(dataSourceMeta.getDsName());
    }

    try {
      PreparedStatement pst = getPreparedStatement(DEFAULT_PRIMARY_KAY, sql, paras);
      result = pst.executeUpdate();
      dataSourceMeta.close(pst);
    } catch (SQLException e) {
      throw new DBException(e);
    }
    return result;
  }


  /**
   * @param sql an SQL statement
   * @see #update(String, Object...)
   */
  public int update(String sql) {
    return update(sql, NULL_PARA_ARRAY);
  }


  public List<Record> find(String sql, Object... paras) {
    List<Record> result = null;
    //hit cache
    if (cached) {
      result = QueryCache.instance().get(dataSourceMeta.getDsName(), sql, paras);
    }
    if (result != null) {
      return result;
    }

    try {
      PreparedStatement pst = getPreparedStatement(DEFAULT_PRIMARY_KAY, sql, paras);
      ResultSet rs = pst.executeQuery();
      result = RecordBuilder.build(dataSourceMeta, rs);
      dataSourceMeta.close(rs, pst);
    } catch (SQLException e) {
      throw new DBException(e);
    }
    //添加缓存
    if (cached) {
      QueryCache.instance().add(dataSourceMeta.getDsName(), sql, paras, result);
    }
    return result;
  }


  /**
   * @param sql the sql statement
   * @see #find(String, Object...)
   */
  public List<Record> find(String sql) {
    return find(sql, NULL_PARA_ARRAY);
  }

  /**
   * Find first record. I recommend add "limit 1" in your sql.
   *
   * @param sql   an SQL statement that may contain one or more '?' IN parameter placeholders
   * @param paras the parameters of sql
   * @return the Record object
   */
  public Record findFirst(String sql, Object... paras) {
    List<Record> result = find(sql, paras);
    return result.size() > 0 ? result.get(0) : null;
  }

  /**
   * @param sql an SQL statement
   * @see #findFirst(String, Object...)
   */
  public Record findFirst(String sql) {
    List<Record> result = find(sql, NULL_PARA_ARRAY);
    return result.size() > 0 ? result.get(0) : null;
  }

  /**
   * Find record by id.
   * Example: Record user = DbPro.use().findById("user", 15);
   *
   * @param tableName the table name of the table
   * @param idValue   the id value of the record
   */
  public Record findById(String tableName, Object idValue) {
    return findById(tableName, DEFAULT_PRIMARY_KAY, idValue);
  }

  /**
   * Find record by id. Fetch the specific columns only.
   * Example: Record user = DbPro.use().findById("user", 15, "name, age");
   *
   * @param tableName the table name of the table
   * @param idValue   the id value of the record
   * @param columns   the specific columns
   */
  public Record findById(String tableName, Number idValue, String... columns) {
    return findById(tableName, DEFAULT_PRIMARY_KAY, idValue, columns);
  }

  /**
   * Find record by id. Fetch the specific columns only.
   * Example: Record user = DbPro.use().findById("user", "user_id", 15, "name, age");
   *
   * @param tableName  the table name of the table
   * @param primaryKey the primary key of the table
   * @param idValue    the id value of the record
   * @param columns    the specific columns separate with comma character ==> ","
   */
  public Record findById(String tableName, String primaryKey, Object idValue, String... columns) {
    String sql = dataSourceMeta.getDialect().select(tableName, "", primaryKey + "=?", columns);
    List<Record> result = find(sql, idValue);
    return result.size() > 0 ? result.get(0) : null;
  }


  /**
   * Delete record by id.
   * Example: boolean succeed = DbPro.use().deleteById("user", "user_id", 15);
   *
   * @param tableName  the table name of the table
   * @param primaryKey the primary key of the table
   * @param id         the id value of the record
   * @return true if delete succeed otherwise false
   */
  public boolean deleteById(String tableName, String primaryKey, Object id) {
    checkNotNull(id, "You can't delete model without Primary Key.");

    String sql = dataSourceMeta.getDialect().delete(tableName, primaryKey + "=?");
    return update(sql, id) >= 1;
  }

  public boolean deleteByIds(String tableName, String[] primaryKeys, Object... ids) {
    checkNotNull(ids, "You can't delete model without Primary Key.");

    String sql = dataSourceMeta.getDialect().delete(tableName, Joiner.on("=?, ").join(ids));
    return update(sql, ids) >= 1;
  }

  /**
   * Delete record by id.
   * Example: boolean succeed = DbPro.use().deleteById("user", 15);
   *
   * @param tableName the table name of the table
   * @param id        the id value of the record
   * @return true if delete succeed otherwise false
   */
  public boolean deleteById(String tableName, Object id) {
    return deleteById(tableName, DEFAULT_PRIMARY_KAY, id);
  }

  /**
   * Delete record.
   * Example: boolean succeed = DbPro.use().delete("user", "id", user);
   *
   * @param tableName  the table name of the table
   * @param primaryKey the primary key of the table
   * @param record     the record
   * @return true if delete succeed otherwise false
   */
  public boolean delete(String tableName, String primaryKey, Record record) {
    return deleteById(tableName, primaryKey, record.get(primaryKey));
  }

  /**
   * Example: boolean succeed = DbPro.use().delete("user", user);
   *
   * @see #delete(String, String, Record)
   */
  public boolean delete(String tableName, Record record) {
    return deleteById(tableName, DEFAULT_PRIMARY_KAY, record.get(DEFAULT_PRIMARY_KAY));
  }

  boolean save(String tableName, String primaryKey, Record record) {
    String sql = dataSourceMeta.getDialect().insert(tableName, record.getAttrNames());
    int result = -1;
    Object[] params = record.getAttrValues();
    //remove cache
    if (cached) {
      QueryCache.instance().purge(dataSourceMeta.getDsName(), tableName);
    }
    PreparedStatement pst = null;
    try {
      pst = getPreparedStatement(primaryKey, sql, params);
      result = pst.executeUpdate();
      getGeneratedKey(pst, primaryKey, record);
    } catch (SQLException e) {
      throw new DBException(e);
    } finally {
      dataSourceMeta.close(pst);
    }
    return result >= 1;
  }

  boolean save(String tableName, String primaryKey, Record... records) {
    return save(tableName, primaryKey, Arrays.asList(records));
  }

  /**
   * 批量保存record
   *
   * @param tableName
   * @param primaryKey
   * @param records
   * @return
   */
  boolean save(String tableName, String primaryKey, List<Record> records) {
    if (records == null || records.size() <= 0) {
      logger.warn("Cloud not found records to save.");
      return false;
    }
    Record firstRecord = records.get(0);
    //清除models缓存
    if (cached) {
      QueryCache.instance().purge(dataSourceMeta.getDsName(), tableName);
    }

    String[] columns = firstRecord.getAttrNames();
    String sql = dataSourceMeta.getDialect().insert(tableName, columns);
    //参数
    Object[][] paras = new Object[records.size()][columns.length];

    for (int i = 0; i < paras.length; i++) {
      for (int j = 0; j < paras[i].length; j++) {
        paras[i][j] = records.get(i).get(columns[j]);
      }
    }

    // --------
    PreparedStatement pst = null;
    int[] result = null;
    Connection connection = null;
    Boolean autoCommit = null;
    try {
      connection = dataSourceMeta.getConnection();
      autoCommit = connection.getAutoCommit();
      if (autoCommit)
        connection.setAutoCommit(false);

      pst = getPreparedStatement(connection, primaryKey, sql, paras);
      result = pst.executeBatch();
      getGeneratedKey(pst, primaryKey, records);
      //没有事务的情况下 手动提交
      if (dataSourceMeta.getCurrentConnection() == null)
        connection.commit();
      connection.setAutoCommit(autoCommit);

      for (int r : result) {
        if (r < 1) {
          return false;
        }
      }
      return true;
    } catch (SQLException e) {
      throw new DBException(e);
    } finally {
      dataSourceMeta.close(pst);
      dataSourceMeta.close(connection);
    }
  }

  /**
   * Get id after insert method getGeneratedKey().
   */
  private void getGeneratedKey(PreparedStatement pst, String primaryKey, Record record) throws SQLException {
    ResultSet rs = pst.getGeneratedKeys();
    if (rs.next())
      record.set(primaryKey, rs.getObject(1));
    rs.close();
  }

  private void getGeneratedKey(PreparedStatement pst, String primaryKey, List<Record> records) throws SQLException {
    ResultSet rs = pst.getGeneratedKeys();
    for (Record record : records) {
      if (record.get(primaryKey) == null) {
        if (rs.next()) {
          record.set(primaryKey, rs.getObject(1));
        }
      }
    }
    rs.close();
  }

  /**
   * @see #save(String, String, Record)
   */
  public boolean save(String tableName, Record record) {
    return save(tableName, DEFAULT_PRIMARY_KAY, record);
  }


  public boolean save(String tableName, Record... records) {
    return save(tableName, DEFAULT_PRIMARY_KAY, records);
  }

  boolean update(String tableName, String primaryKey, Record record) {
    Object id = record.get(primaryKey);
    checkNotNull(id, "You can't update model without Primary Key.");

    String[] attrs = record.getAttrNames();
    String[] columns = new String[attrs.length - 1];
    Object[] paras = new Object[attrs.length];
    int i = 0;
    for (String attr : attrs) {
      if (attr.equals(primaryKey)) {
        continue;
      }
      columns[i] = attr;
      paras[i] = record.get(attr);
      i++;
    }
    paras[i] = id;

    String sql = dataSourceMeta.getDialect().update(tableName, "", primaryKey + "=?", columns);

    return update(sql, paras) >= 1;
  }

  /**
   * Update Record. The primary key of the table is: "id".
   *
   * @see #update(String, String, Record)
   */
  public boolean update(String tableName, Record record) {
    return update(tableName, DEFAULT_PRIMARY_KAY, record);
  }

  /**
   * 分页查询Record
   *
   * @param pageNumber 页码
   * @param pageSize   页大小
   * @param sql        sql
   * @param paras      参数
   * @return page
   */
  public Page<Record> paginate(int pageNumber, int pageSize, String sql, Object... paras) {
    checkArgument(pageNumber >= 1 || pageSize >= 1, "pageNumber and pageSize must be more than 0");

    Dialect dialect = dataSourceMeta.getDialect();

    long totalRow = 0;
    int totalPage = 0;
    List result = query(dialect.countWith(sql), paras);
    int size = result.size();
    if (size == 1)
      totalRow = ((Number) result.get(0)).longValue();
    else if (size > 1)
      totalRow = result.size();
    else
      return new Page<Record>(new ArrayList<Record>(0), pageNumber, pageSize, 0, 0);

    totalPage = (int) (totalRow / pageSize);
    if (totalRow % pageSize != 0) {
      totalPage++;
    }

    // --------
    List<Record> list = find(dialect.paginateWith(pageNumber, pageSize, sql), paras);
    return new Page<Record>(list, pageNumber, pageSize, totalPage, (int) totalRow);
  }


  /**
   * @see #paginate(int, int, String, Object...)
   */
  public Page<Record> paginate(int pageNumber, int pageSize, String sql) {
    return paginate(pageNumber, pageSize, sql, NULL_PARA_ARRAY);
  }


  public boolean excute(String... sqls) {
    return excute(Arrays.asList(sqls));
  }

  /**
   * Execute a batch of SQL INSERT, UPDATE, or DELETE queries.
   * Example:
   * <pre>
   * int[] result = DbPro.use().batch("myConfig", sqlList, 500);
   * </pre>
   *
   * @param sqls The SQL list to execute.
   * @return The number of rows updated per statement
   */
  public boolean excute(List<String> sqls) {

    Statement stmt = null;
    int[] result = null;
    Connection connection = null;
    Boolean autoCommit = null;
    try {
      connection = dataSourceMeta.getConnection();
      autoCommit = connection.getAutoCommit();
      if (autoCommit)
        connection.setAutoCommit(false);

      stmt = getPreparedStatement(connection, sqls);
      result = stmt.executeBatch();
      //没有事务的情况下 手动提交
      if (dataSourceMeta.getCurrentConnection() == null)
        connection.commit();
      connection.setAutoCommit(autoCommit);

      for (int r : result) {
        if (r < 1) {
          return false;
        }
      }
      return true;
    } catch (SQLException e) {
      throw new DBException(e);
    } finally {
      dataSourceMeta.close(stmt);
      dataSourceMeta.close(connection);
    }
  }
}



