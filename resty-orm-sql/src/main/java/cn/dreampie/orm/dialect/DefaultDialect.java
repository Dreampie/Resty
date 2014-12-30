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


package cn.dreampie.orm.dialect;


import cn.dreampie.util.Joiner;


/**
 * @author Igor Polevoy
 */
public abstract class DefaultDialect implements Dialect {

  public String select(String from) {
    return "SELECT * FROM " + from;
  }

  public String select(String from, String where) {
    return where != null ? "SELECT * FROM " + from + " WHERE " + where : select(from);
  }

  protected void appendQuestions(StringBuilder query, int count) {
    Joiner joiner = Joiner.on(", ");
    for (int i = 0; i < count; i++) {
      query.append(joiner.join(query, "?"));
    }
  }

  public String insert(String table, String... columns) {
    StringBuilder query = new StringBuilder().append("INSERT INTO ").append(table).append(" (");
    query.append(Joiner.on(", ").join(query, columns));
    query.append(") VALUES (");
    appendQuestions(query, columns.length);
    query.append(')');
    return query.toString();
  }


  public String delete(String table) {
    return "DELETE FROM " + table;
  }


  public String delete(String table, String where) {
    return "DELETE FROM " + table + " WHERE " + where;
  }


  public String update(String table, String set) {
    return "UPDATE FROM " + table + " SET " + set;
  }


  public String update(String table, String set, String where) {
    return "UPDATE FROM " + table + " SET " + set + " WHERE " + where;
  }


  public String count(String table) {
    return "SELECT COUNT(*) FROM " + table;
  }


  public String count(String table, String where) {
    return "SELECT COUNT(*) FROM " + table + " WHERE " + where;
  }
}
