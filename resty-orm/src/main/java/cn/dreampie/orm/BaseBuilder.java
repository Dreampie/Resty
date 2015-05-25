package cn.dreampie.orm;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ice on 14-12-30.
 */
public class BaseBuilder {
//  private static final Logger logger = Logger.getLogger(BaseBuilder.class);

  public static <T> List<T> build(ResultSet rs, Class<? extends Base> modelClass, DataSourceMeta dataSourceMeta, TableMeta tableMeta) throws SQLException, InstantiationException, IllegalAccessException {
    List<T> result = new ArrayList<T>();
    ResultSetMetaData rsmd = rs.getMetaData();
    int columnCount = rsmd.getColumnCount();
    String[] labelNames = new String[columnCount + 1];
    int[] types = new int[columnCount + 1];
    buildLabelNamesAndTypes(rsmd, labelNames, types);

    Base entity;
    Object value;

    while (rs.next()) {
      if (Record.class.isAssignableFrom(modelClass)) {
        entity = new Record(tableMeta);
      } else {
        entity = modelClass.newInstance();
      }
      for (int i = 1; i <= columnCount; i++) {
        value = rs.getObject(i);
        entity.init(labelNames[i], value);
      }
      result.add((T) entity);
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
