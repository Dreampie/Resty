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

import cn.dreampie.orm.dialect.Dialect;

import java.sql.Connection;

/**
 * ConnectionAccess
 */
public class ConnectionMetadata {

  private String dbName;
  private Connection connection;
  private Dialect dialect;

  public ConnectionMetadata(String dbName, Connection connection, Dialect dialect) {
    this.dbName = dbName;
    this.connection = connection;
    this.dialect = dialect;
  }

  public String getDbName() {
    return dbName;
  }

  public Connection getConnection() {
    return connection;
  }

  public Dialect getDialect() {
    return dialect;
  }
}
