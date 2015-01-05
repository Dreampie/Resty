package cn.dreampie.orm;

import cn.dreampie.log.Logger;
import cn.dreampie.log.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.SortedMap;

/**
 * Created by wangrenhui on 14/12/30.
 */
public class ModelMetaBuilder {

  private static final Logger logger = LoggerFactory.getLogger(ModelMetaBuilder.class);

  static void build(List<ModelMeta> modelMetas, DataSourceMeta dsm) {
    ModelMeta temp = null;
    Connection conn = null;
    try {
      conn = dsm.getDataSource().getConnection();
      for (ModelMeta modelMeta : modelMetas) {
        temp = modelMeta;
        modelMeta.setColumnMetadata(fetchMetaParams(conn.getMetaData(), conn.getMetaData().getDatabaseProductName(), modelMeta.getTableName()));
        //添加到model元数据集合
        Metadatas.addModelMeta(modelMeta.getModelClass(), modelMeta);
      }
    } catch (Exception e) {
      if (temp != null)
        throw new RuntimeException("Could not create Table object, maybe the table " + temp.getTableName() + " is not exists.", e);
    } finally {
      dsm.close(conn);
    }
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
      String[] vals = table.split(".");

      if (vals.length == 1) {
        tableName = vals[0];
      } else if (vals.length == 2) {
        schema = vals[0];
        tableName = vals[1];
        if (schema.length() == 0 || tableName.length() == 0) {
          throw new RuntimeException("Invalid table name : " + table);
        }
      } else {
        throw new RuntimeException("Invalid table name: " + table);
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
      logger.info("Fetched metadata for table: %s", table);
    } else {
      logger.warn("Failed to retrieve metadata for table: '%s'."
              + " Are you sure this table exists? For some databases table name are case sensitive.",
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

      ColumnMeta cm = new ColumnMeta(rs.getString("COLUMN_NAME"), rs.getString("TYPE_NAME"), rs.getInt("COLUMN_SIZE"));
      columns.put(cm.getColumnName(), cm);
    }
    return columns;
  }

}
