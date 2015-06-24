package cn.dreampie.orm.callback;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Callback.
 */
public interface Resultback {

  /**
   * Place codes here that need call back by callableStatement.
   *
   * @param rs callableStatement
   */
  <T> List<T> call(ResultSet rs) throws SQLException;
}