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


package cn.dreampie.orm.cache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * StatementCache
 */
enum StatementCache {
  INSTANCE;

  static StatementCache instance() {
    return INSTANCE;
  }

  private final ConcurrentMap<Connection, Map<String, PreparedStatement>> statementCache = new ConcurrentHashMap<Connection, Map<String, PreparedStatement>>();

  private StatementCache() {
  }

  PreparedStatement getPreparedStatement(Connection connection, String query) {
    if (!statementCache.containsKey(connection)) {
      statementCache.put(connection, new HashMap<String, PreparedStatement>());
    }
    return statementCache.get(connection).get(query);
  }

  public void cache(Connection connection, String query, PreparedStatement ps) {
    statementCache.get(connection).put(query, ps);
  }

  void cleanStatementCache(Connection connection) {
    Map<String, PreparedStatement> stmsMap = statementCache.remove(connection);
    if (stmsMap != null) { //Close prepared statements to release cursors on connection pools
      for (PreparedStatement stmt : stmsMap.values()) {
        closeQuietly(stmt);
      }
    }
  }

  static void closeQuietly(Statement st) {
    try {
      if (st != null) {
        st.close();
      }
    } catch (SQLException e) {
    }
  }
}
