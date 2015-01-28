package cn.dreampie.orm;

import java.sql.CallableStatement;
import java.sql.SQLException;

/**
 * InCall.
 */
public interface InCall {

  /**
   * Place codes here that need call back by callableStatement.
   *
   * @param cstmt callableStatement
   */
  Object call(CallableStatement cstmt) throws SQLException;
}