package cn.dreampie.orm.callback;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ice on 14-12-30.
 */
public class ListMapback implements Resultback {

  public List<Map<String, Object>> call(ResultSet rs) throws SQLException {
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    ResultSetMetaData rsmd = rs.getMetaData();
    int columnCount = rsmd.getColumnCount();
    String[] labelNames = new String[columnCount + 1];
    int[] types = new int[columnCount + 1];
    buildLabelNamesAndTypes(rsmd, labelNames, types);

    Map<String, Object> map;

    while (rs.next()) {
      map = new HashMap<String, Object>();
      for (int i = 1; i <= columnCount; i++) {
        map.put(labelNames[i], rs.getObject(i));
      }
      result.add(map);
    }
    return result;
  }

  private void buildLabelNamesAndTypes(ResultSetMetaData rsmd, String[] labelNames, int[] types) throws SQLException {
    for (int i = 1; i < labelNames.length; i++) {
      labelNames[i] = rsmd.getColumnLabel(i);
      types[i] = rsmd.getColumnType(i);
    }
  }

}
