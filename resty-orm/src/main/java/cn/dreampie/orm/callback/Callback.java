package cn.dreampie.orm.callback;

import java.sql.CallableStatement;
import java.sql.SQLException;

/**
 * Callback.
 */
public interface Callback {

  /**
   * Place codes here that need call back by callableStatement.
   *
   * @param cs callableStatement
   */
  <T> T call(CallableStatement cs) throws SQLException;
}