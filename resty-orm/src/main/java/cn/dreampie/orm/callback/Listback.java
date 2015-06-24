package cn.dreampie.orm.callback;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ice on 14-12-30.
 */
public class Listback implements Resultback {

  public <T> List<T> call(ResultSet rs) throws SQLException {
    List<T> result = new ArrayList<T>();
    int colAmount = rs.getMetaData().getColumnCount();
    if (colAmount > 1) {
      while (rs.next()) {
        Object[] temp = new Object[colAmount];
        for (int i = 0; i < colAmount; i++) {
          temp[i] = rs.getObject(i + 1);
        }
        result.add((T) temp);
      }
    } else if (colAmount == 1) {
      while (rs.next()) {
        result.add((T) rs.getObject(1));
      }
    }
    return result;
  }
}
