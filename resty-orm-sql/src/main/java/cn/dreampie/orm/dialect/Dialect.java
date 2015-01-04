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

/**
 * @author ericbn
 */
public interface Dialect {
  public String getDbType();

  public String validQuery();

  public String select(String table);

  public String select(String table, String... columns);

  public String select(String table, String where);

  public String select(String table, String where, String... columns);

  public String insert(String table, String... columns);

  public String delete(String table);

  public String delete(String table, String where);

  public String update(String table, String... columns);

  public String update(String table, String where, String... columns);

  public String count(String table);

  public String count(String table, String where);

  public String countWith(String sql);

  public String paginate(int pageNo, int pageSize, String table);

  public String paginate(int pageNo, int pageSize, String table, String... columns);

  public String paginate(int pageNo, int pageSize, String table, String where);

  public String paginate(int pageNo, int pageSize, String table, String where, String... columns);

  public String paginateWith(int pageNo, int pageSize, String sql);
}
