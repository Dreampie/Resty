/*
Copyright 2009-2014 Igor Polevoy

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License. 
*/


package cn.dreampie.orm;

import cn.dreampie.log.Logger;
import cn.dreampie.log.LoggerFactory;
import cn.dreampie.orm.dialect.Dialect;
import cn.dreampie.orm.exception.DBException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static cn.dreampie.util.Checker.checkNotNull;

/**
 * ConnectionAccess
 */
public class DataSourceMeta {

  private static final Logger logger = LoggerFactory.getLogger(DataSourceMeta.class);

  private boolean showSql;
  private String dsName;
  private DataSource dataSource;
  private Dialect dialect;

  private final ThreadLocal<Connection> connectionTL = new ThreadLocal<Connection>();

  public DataSourceMeta(DataSourceProvider dataSourceProvider) {
    this(DS.DEFAULT_DS_NAME, dataSourceProvider.getDataSource(), dataSourceProvider.getDialect(), false);
  }

  public DataSourceMeta(String dsName, DataSourceProvider dataSourceProvider) {
    this(dsName, dataSourceProvider.getDataSource(), dataSourceProvider.getDialect(), false);
  }

  public DataSourceMeta(String dsName, DataSourceProvider dataSourceProvider, boolean showSql) {
    this(dsName, dataSourceProvider.getDataSource(), dataSourceProvider.getDialect(), showSql);
  }

  public DataSourceMeta(DataSource dataSource, Dialect dialect) {
    this(DS.DEFAULT_DS_NAME, dataSource, dialect, false);
  }

  public DataSourceMeta(DataSource dataSource, Dialect dialect, boolean showSql) {
    this(DS.DEFAULT_DS_NAME, dataSource, dialect, showSql);
  }

  public DataSourceMeta(String dsName, DataSource dataSource, Dialect dialect) {
    this(dsName, dataSource, dialect, false);
  }

  public DataSourceMeta(String dsName, DataSource dataSource, Dialect dialect, boolean showSql) {
    this.dsName = checkNotNull(dsName);
    this.dataSource = checkNotNull(dataSource);
    this.dialect = checkNotNull(dialect);
    this.showSql = showSql;
  }

  public String getDsName() {
    return dsName;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public Dialect getDialect() {
    return dialect;
  }

  public Connection getConnection() throws SQLException {
    Connection conn = connectionTL.get();
    if (conn != null)
      return conn;
    return showSql ? new SqlPrinter(dataSource.getConnection()).getConnection() : dataSource.getConnection();
  }

  public Connection getCurrentConnection() {
    return connectionTL.get();
  }

  public void setCurrentConnection(Connection connection) {
    connectionTL.set(connection);
  }

  public void rmCurrentConnection() {
    connectionTL.remove();
  }

  /**
   * Close ResultSet、Statement、Connection
   * ThreadLocal support declare transaction.
   */
  public final void close(ResultSet rs, Statement st) {
    if (rs != null) {
      try {
        rs.close();
      } catch (SQLException e) {
        logger.warn("Could not close resultSet!", e);
      }
    }

    close(st);
  }

  public final void close(Statement st) {
    if (st != null) {
      try {
        st.close();
      } catch (SQLException e) {
        logger.warn("Could not close statement!", e);
      }
    }

    try {
      close(getConnection());
    } catch (SQLException e) {
      throw new DBException(e);
    }
  }

  public final void close(Connection conn) {
    if (connectionTL.get() == null) {   // in transaction if conn in threadlocal
      if (conn != null)
        try {
          conn.close();
        } catch (SQLException e) {
          logger.warn("Could not close connection!", e);
        }
    }
  }
}
