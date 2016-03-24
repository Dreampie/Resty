package cn.dreampie.orm;

import cn.dreampie.common.entity.CaseInsensitiveMap;
import cn.dreampie.log.Logger;
import cn.dreampie.orm.exception.DBException;

import java.net.ConnectException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.SortedMap;

/**
 * Created by wangrenhui on 14/12/30.
 */
public class TableMetaBuilder {

  private static final Logger logger = Logger.getLogger(TableMetaBuilder.class);

  public static Set<TableMeta> buildTableMeta(Set<TableMeta> tableMetas, DataSourceMeta dsm) {
    TableMeta temp = null;
    Connection conn = null;
    try {
      conn = dsm.getWriteDataSource().getConnection();
      for (TableMeta tableMeta : tableMetas) {
        temp = tableMeta;
        temp.setColumnMetadata(fetchMetaParams(conn.getMetaData(), conn.getMetaData().getDatabaseProductName(), tableMeta.getTableName()));
        //添加到model元数据集合
        Metadata.addTableMeta(temp);
      }
    } catch (Exception e) {
      logAcess(dsm, temp, e);
    } finally {
      dsm.close(conn);
    }
    return tableMetas;
  }

  private static void logAcess(DataSourceMeta dsm, TableMeta temp, Exception e) {

    String message = e.getMessage();
    if (e instanceof ConnectException) {
      message = "Could not connect dataSource for name '" + dsm.getWriteDsName() + "'";
    } else {
      if (temp != null) {
        message = "Could not create table object, maybe the table " + temp.getTableName() + " is not exists.";
      }
    }

    if (message == null) {
      Throwable throwable = e.getCause();
      if (throwable != null) {
        message = throwable.getMessage();
      }
    }
    throw new DBException(message, e);
  }

  public static TableMeta buildTableMeta(TableMeta tableMeta, DataSourceMeta dsm) {
    Connection conn = null;
    try {
      conn = dsm.getWriteDataSource().getConnection();
      tableMeta.setColumnMetadata(fetchMetaParams(conn.getMetaData(), conn.getMetaData().getDatabaseProductName(), tableMeta.getTableName()));
      //添加到record元数据集合
      Metadata.addTableMeta(tableMeta);
    } catch (Exception e) {
      logAcess(dsm, tableMeta, e);
    } finally {
      dsm.close(conn);
    }
    return tableMeta;
  }


  /**
   * Returns a hash keyed off a column name.
   *
   * @return
   * @throws java.sql.SQLException
   */
  private static SortedMap<String, ColumnMeta> fetchMetaParams(DatabaseMetaData databaseMetaData, String databaseProductName, String table) throws SQLException {
    // Valid table name format: tablename or schemaname.tablename
    String schema = null;
    String tableName;

    if (table.contains(".")) {
      String[] vals = table.split("\\.");

      if (vals.length == 1) {
        tableName = vals[0];
      } else if (vals.length == 2) {
        schema = vals[0];
        tableName = vals[1];
        if (schema.length() == 0 || tableName.length() == 0) {
          throw new DBException("Invalid table name : " + table);
        }
      } else {
        throw new DBException("Invalid table name: " + table);
      }
    } else {
      tableName = table;
    }

    ResultSet rs = databaseMetaData.getColumns(null, schema, tableName, null);
    String dbProduct = databaseMetaData.getDatabaseProductName().toLowerCase();
    SortedMap<String, ColumnMeta> columns = getColumns(rs, dbProduct);
    rs.close();

    //try upper case table name - Oracle uses upper case
    if (columns.isEmpty()) {
      rs = databaseMetaData.getColumns(null, schema, tableName.toUpperCase(), null);
      dbProduct = databaseProductName.toLowerCase();
      columns = getColumns(rs, dbProduct);
      rs.close();
    }

    //if upper case not found, try lower case.
    if (columns.isEmpty()) {
      rs = databaseMetaData.getColumns(null, schema, tableName.toLowerCase(), null);
      columns = getColumns(rs, dbProduct);
      rs.close();
    }

    if (columns.size() > 0) {
      logger.debug("Fetched metadata for table: %s", table);
    } else {
      logger.warn("Failed to retrieve metadata for table: '%s'. Are you sure this table exists? For some databases table name are case sensitive.", table);
    }
    return columns;
  }


  private static SortedMap<String, ColumnMeta> getColumns(ResultSet rs, String dbProduct) throws SQLException {

    SortedMap<String, ColumnMeta> columns = new CaseInsensitiveMap<ColumnMeta>();
    while (rs.next()) {
      if (dbProduct.equals("h2") && "INFORMATION_SCHEMA".equals(rs.getString("TABLE_SCHEMA"))) {
        continue; // skip h2 INFORMATION_SCHEMA table columns.
      }

      ColumnMeta cm = new ColumnMeta(rs.getString("COLUMN_NAME"), rs.getString("TYPE_NAME"), rs.getInt("DATA_TYPE"), rs.getInt("COLUMN_SIZE"));
      columns.put(cm.getColumnName(), cm);
    }
    return columns;
  }


}
