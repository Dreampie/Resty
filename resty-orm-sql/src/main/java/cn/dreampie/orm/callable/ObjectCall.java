package cn.dreampie.orm.callable;

import java.sql.CallableStatement;
import java.sql.SQLException;

/**
 * ObjectCall.
 */
public interface ObjectCall {

  /**
   * Place codes here that need call back by callableStatement.
   *
   * @param cstmt callableStatement
   */
  Object call(CallableStatement cstmt) throws SQLException;
}