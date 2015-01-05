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


import cn.dreampie.common.Constant;
import cn.dreampie.log.Logger;
import cn.dreampie.util.Joiner;

import java.util.Arrays;


/**
 * This is a main cache facade. It could be architected in the future to add more cache implementations besides OSCache.
 */
public enum QueryCache {
  INSTANCE;

  private final static Logger logger = Logger.getLogger(QueryCache.class);

  private final boolean enabled = Constant.cache_enabled;

  private final CacheManager cacheManager;

  //singleton

  private QueryCache() {
    cacheManager = Constant.cacheManager;
  }


  /**
   * This class is a singleton, get an instance with this method.
   *
   * @return one and only one instance of this class.
   */
  public static QueryCache instance() {
    return INSTANCE;
  }

  /**
   * Adds an item to cache. Expected some lists of objects returned from "select" queries.
   *
   * @param tableName - name of table.
   * @param query     query text
   * @param params    - list of parameters for a query.
   * @param cache     object to cache.
   */
  public void addItem(String dsName, String tableName, String query, Object[] params, Object cache) {
    if (enabled) {
      cacheManager.addCache(getGroup(dsName, tableName), getKey(dsName, tableName, query, params), cache);
    }
  }

  private String getGroup(String dsName, String tableName) {
    return dsName + "." + tableName;
  }

  /**
   * Returns an item from cache, or null if nothing found.
   *
   * @param tableName name of table.
   * @param query     query text.
   * @param params    list of query parameters, can be null if no parameters are provided.
   * @return cache object or null if nothing found.
   */
  public Object getItem(String dsName, String tableName, String query, Object[] params) {

    if (enabled) {
      String key = getKey(dsName, tableName, query, params);
      Object item = cacheManager.getCache(getGroup(dsName, tableName), key);
      if (item == null) {
        logAccess(getGroup(dsName, tableName), query, params, "Miss");
      } else {
        logAccess(getGroup(dsName, tableName), query, params, "Hit");
      }
      return item;
    } else {
      return null;
    }
  }

  static void logAccess(String group, String query, Object[] params, String access) {
    if (logger.isInfoEnabled()) {
      StringBuilder log = new StringBuilder().append(access).append(", ").append('"').append(group + "." + query).append('"');
      if (params != null && params.length > 0) {
        log.append(", with parameters: ").append('<');
        Joiner.on(">, <").join(log, params);
        log.append('>');
      }
      logger.info(log.toString());
    }
  }


  private String getKey(String dsName, String tableName, String query, Object[] params) {
    return dsName + "." + tableName + "." + query + "." + (params == null ? null : Arrays.asList(params).toString());
  }

  /**
   * This method purges (removes) all caches associated with a table, if caching is enabled and
   * a corresponding model is marked cached.
   *
   * @param tableName table name whose caches are to be purged.
   */
  public void purgeTableCache(String dsName, String tableName) {
    if (enabled) {
      cacheManager.flush(new CacheEvent(getGroup(dsName, tableName), getClass().getName()));
    }
  }

  public CacheManager getCacheManager() {
    return cacheManager;
  }
}

