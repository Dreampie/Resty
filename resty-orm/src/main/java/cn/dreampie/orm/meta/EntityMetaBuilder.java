package cn.dreampie.orm.meta;

import cn.dreampie.common.entity.CaseInsensitiveMap;
import cn.dreampie.log.Logger;
import cn.dreampie.orm.Metadata;
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
public class EntityMetaBuilder {

  private static final Logger logger = Logger.getLogger(EntityMetaBuilder.class);

  public static Set<EntityMeta> buildColumnMeta(Set<EntityMeta> entityMetas, DataSourceMeta dsm) {
    EntityMeta temp = null;
    Connection conn = null;
    try {
      conn = dsm.getDataSource().getConnection();
      for (EntityMeta entityMeta : entityMetas) {
        temp = entityMeta;
        temp.setColumnMetas(fetchColumnMeta(conn.getMetaData(), conn.getMetaData().getDatabaseProductName(), entityMeta.getTable()));
      }
    } catch (Exception e) {
      logAcess(dsm, temp, e);
    } finally {
      dsm.close(conn);
    }
    return entityMetas;
  }

  private static void logAcess(DataSourceMeta dsm, EntityMeta temp, Exception e) {

    String message = e.getMessage();
    if (e instanceof ConnectException) {
      message = "Could not connect dataSource for name '" + dsm.getDsName() + "'";
    } else {
      if (temp != null) {
        message = "Could not create table meta, maybe the table '" + temp.getTable() + "' is not exists.";
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

  public static EntityMeta buildColumnMeta(EntityMeta entityMeta, DataSourceMeta dsm) {
    Connection conn = null;
    try {
      conn = dsm.getDataSource().getConnection();
      entityMeta.setColumnMetas(fetchColumnMeta(conn.getMetaData(), conn.getMetaData().getDatabaseProductName(), entityMeta.getTable()));
      //添加到record元数据集合
      Metadata.addEntityMeta(entityMeta);
    } catch (Exception e) {
      logAcess(dsm, entityMeta, e);
    } finally {
      dsm.close(conn);
    }
    return entityMeta;
  }


  /**
   * Returns a hash keyed off a column table.
   *
   * @return
   * @throws java.sql.SQLException
   */
  private static SortedMap<String, ColumnMeta> fetchColumnMeta(DatabaseMetaData databaseMetaData, String databaseProductName, String table) throws SQLException {
    // Valid table table format: tablename or schemaname.tablename
    String schema = null;
    String tableName;

    if (table.contains(".")) {
      String[] vals = table.split(".");

      if (vals.length == 1) {
        tableName = vals[0];
      } else if (vals.length == 2) {
        schema = vals[0];
        tableName = vals[1];
        if (schema.length() == 0 || tableName.length() == 0) {
          throw new DBException("Invalid table table : " + table);
        }
      } else {
        throw new DBException("Invalid table table: " + table);
      }
    } else {
      tableName = table;
    }

    ResultSet rs = databaseMetaData.getColumns(null, schema, tableName, null);
    String dbProduct = databaseMetaData.getDatabaseProductName().toLowerCase();
    SortedMap<String, ColumnMeta> columns = getColumns(rs, dbProduct);
    rs.close();

    //try upper case table table - Oracle uses upper case
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
      logger.warn("Failed to retrieve metadata for table: '%s'."
              + " Are you sure this table exists? For some databases table table are case sensitive.",
          table);
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
