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

import cn.dreampie.orm.ModelMetadata;

import java.sql.PreparedStatement;
import java.util.List;

/**
 * @author ericbn
 */
public interface Dialect {
  String getDbType();

  String select(String from);

  String select(String from, String where);

  String insert(String table, String... columns);

  String delete(String table);

  String delete(String table, String where);

  String update(String table, String set);

  String update(String table, String set, String where);

  String count(String table);

  String count(String table, String where);
}
