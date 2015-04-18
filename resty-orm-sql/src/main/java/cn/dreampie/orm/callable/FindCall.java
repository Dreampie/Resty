package cn.dreampie.orm.callable;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * InCall.
 */
public interface FindCall {

  /**
   * Place codes here that need call back by callableStatement.
   *
   * @param cstmt callableStatement
   */
  ResultSet call(CallableStatement cstmt) throws SQLException;
}