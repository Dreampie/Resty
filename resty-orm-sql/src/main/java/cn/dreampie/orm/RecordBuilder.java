package cn.dreampie.orm;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by ice on 14-12-30.
 */
public class RecordBuilder {
  public static List<Record> build(ResultSet rs, DataSourceMeta dataSourceMeta, TableMeta tableMeta) throws SQLException {
    List<Record> result = new ArrayList<Record>();
    ResultSetMetaData rsmd = rs.getMetaData();
    int columnCount = rsmd.getColumnCount();
    String[] labelNames = new String[columnCount + 1];
    int[] types = new int[columnCount + 1];
    buildLabelNamesAndTypes(rsmd, labelNames, types);

    Record record;
    Map<String, Object> columns;
    Object value;
    while (rs.next()) {
      record = Record.useDS(dataSourceMeta, tableMeta);
      columns = record.getAttrs();
      for (int i = 1; i <= columnCount; i++) {

        if (types[i] < Types.BLOB)
          value = rs.getObject(i);
        else if (types[i] == Types.CLOB)
          value = ModelBuilder.handleClob(rs.getClob(i));
        else if (types[i] == Types.NCLOB)
          value = ModelBuilder.handleClob(rs.getNClob(i));
        else if (types[i] == Types.BLOB)
          value = ModelBuilder.handleBlob(rs.getBlob(i));
        else
          value = rs.getObject(i);

        columns.put(labelNames[i], value);
      }
      result.add(record);
    }
    return result;
  }

  private static void buildLabelNamesAndTypes(ResultSetMetaData rsmd, String[] labelNames, int[] types) throws SQLException {
    for (int i = 1; i < labelNames.length; i++) {
      labelNames[i] = rsmd.getColumnLabel(i);
      types[i] = rsmd.getColumnType(i);
    }
  }
}
