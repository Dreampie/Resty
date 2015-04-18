package cn.dreampie.orm.callable;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * ResultSetCall.
 */
public interface ResultSetCall {

  /**
   * Place codes here that need call back by callableStatement.
   *
   * @param cstmt callableStatement
   */
  ResultSet call(CallableStatement cstmt) throws SQLException;
}