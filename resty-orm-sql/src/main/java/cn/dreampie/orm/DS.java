/**
 * Copyright (c) 2011-2015, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.dreampie.orm;


import cn.dreampie.orm.dialect.Dialect;
import cn.dreampie.orm.exception.ActiveRecordException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static cn.dreampie.util.Checker.checkNotNull;

/**
 * DS. Professional database query and update tool.
 */
public class DS {

  public static final String DEFAULT_DS_NAME = "default";
  public static final String DEFAULT_PRIMARY_KAY = "id";
  public static final Object[] NULL_PARA_ARRAY = new Object[0];

  private DataSourceMeta dataSourceMeta;

  public static DS use() {
    return DS.use(DEFAULT_DS_NAME);
  }

  public static DS use(String dbName) {
    DS ds = new DS();
    ds.dataSourceMeta = Metadatas.getDataSourceMeta(dbName);
    checkNotNull(ds.dataSourceMeta, " Not found dbName:" + dbName);
    return ds;
  }

  private PreparedStatement getPreparedStatement(String sql, Object[] paras) throws SQLException {
    PreparedStatement pst = dataSourceMeta.getConnection().prepareStatement(sql);
    for (int i = 0; i < paras.length; i++) {
      pst.setObject(i + 1, paras[i]);
    }
    return pst;
  }

  <T> List<T> query(String sql, Object... paras) {

    List result = new ArrayList();
    try {

      PreparedStatement pst = getPreparedStatement(sql, paras);
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
      throw new RuntimeException(e);
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
        throw new RuntimeException("Only one column can be queried.");
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
    try {
      PreparedStatement pst = getPreparedStatement(sql, paras);
      result = pst.executeUpdate();
      dataSourceMeta.close(pst);
    } catch (SQLException e) {
      throw new RuntimeException(e);
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

  /**
   * Get id after insert method getGeneratedKey().
   */
  private Object getGeneratedKey(PreparedStatement pst) throws SQLException {
    ResultSet rs = pst.getGeneratedKeys();
    Object id = null;
    if (rs.next())
      id = rs.getObject(1);
    rs.close();
    return id;
  }


  public List<Record> find(String sql, Object... paras) {
    List<Record> result = null;
    try {
      PreparedStatement pst = getPreparedStatement(sql, paras);
      ResultSet rs = pst.executeQuery();
      result = RecordBuilder.build(dataSourceMeta, rs);
      dataSourceMeta.close(rs, pst);
    } catch (SQLException e) {
      throw new ActiveRecordException(e);
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
   * Find record by id.
   * Example: Record user = DbPro.use().findById("user", "user_id", 15);
   *
   * @param tableName  the table name of the table
   * @param primaryKey the primary key of the table
   * @param idValue    the id value of the record
   */
  public Record findById(String tableName, String primaryKey, Number idValue) {
    String sql = dataSourceMeta.getDialect().select(tableName, primaryKey + "=?");
    List<Record> result = find(sql, idValue);
    return result.size() > 0 ? result.get(0) : null;
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
    String sql = dataSourceMeta.getDialect().select(tableName, primaryKey + "=?", columns);
    List<Record> result = find(sql, idValue);
    return result.size() > 0 ? result.get(0) : null;
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
    String sql = dataSourceMeta.getDialect().insert(tableName, record.getColumnNames());
    int result = -1;
    PreparedStatement pst = null;
    try {
      pst = getPreparedStatement(sql, record.getColumnValues());
      result = pst.executeUpdate();
      record.set(primaryKey, getGeneratedKey(pst));
    } catch (SQLException e) {
      throw new ActiveRecordException(e);
    } finally {
      dataSourceMeta.close(pst);
    }
    return result >= 1;
  }


  /**
   * @see #save(String, String, Record)
   */
  public boolean save(String tableName, Record record) {
    return save(tableName, DEFAULT_PRIMARY_KAY, record);
  }

  boolean update(String tableName, String primaryKey, Record record) {
    Object id = record.get(primaryKey);
    checkNotNull(id, "You can't update model without Primary Key.");

    String sql = dataSourceMeta.getDialect().update(tableName, primaryKey, record.getColumnNames());

    return update(sql, record.getColumnValues()) >= 1;
  }

  /**
   * Update Record. The primary key of the table is: "id".
   *
   * @see #update(String, String, Record)
   */
  public boolean update(String tableName, Record record) {
    return update(tableName, DEFAULT_PRIMARY_KAY, record);
  }


  public Page<Record> paginate(int pageNo, int pageSize, String sql, Object... paras) {
    if (pageNo < 1 || pageSize < 1)
      throw new ActiveRecordException("pageNo and pageSize must be more than 0");
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
      return new Page<Record>(new ArrayList<Record>(0), pageNo, pageSize, 0, 0);

    totalPage = (int) (totalRow / pageSize);
    if (totalRow % pageSize != 0) {
      totalPage++;
    }

    // --------
    List<Record> list = find(dialect.paginateWith(pageNo, pageSize, sql), paras);
    return new Page<Record>(list, pageNo, pageSize, totalPage, (int) totalRow);
  }


  /**
   * @see #paginate(int, int, String, Object...)
   */
  public Page<Record> paginate(int pageNo, int pageSize, String sql) {
    return paginate(pageNo, pageSize, sql, NULL_PARA_ARRAY);
  }


}



