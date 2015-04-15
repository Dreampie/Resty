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


package cn.dreampie.security.cache;


import cn.dreampie.cache.CacheEvent;
import cn.dreampie.cache.CacheManager;
import cn.dreampie.common.Constant;
import cn.dreampie.log.Logger;


/**
 * This is a main cache facade. It could be architected in the future to add more cache implementations besides OSCache.
 */
public enum SessionCache {
  INSTANCE;

  private final static Logger logger = Logger.getLogger(SessionCache.class);

  private final boolean enabled = Constant.cacheEnabled;

  private final CacheManager cacheManager;

  private final static String CONNECTOR = "#";
  //singleton

  private SessionCache() {
    cacheManager = CacheManager.MANAGER;
  }


  /**
   * This class is a singleton, get an instance with this method.
   *
   * @return one and only one instance of this class.
   */
  public static SessionCache instance() {
    return INSTANCE;
  }

  /**
   * Adds an item to cache. Expected some lists of objects returned from "select" queries.
   *
   * @param group - key of cache type  principal or  session.
   * @param key   -  key of  cache
   * @param value object to cache.
   */
  public void add(String group, String key, Object value) {
    if (enabled) {
      cacheManager.addCache(group, key, value);
    }
  }


  /**
   * Returns an item from cache, or null if nothing found.
   *
   * @param group - key of cache type  principal or  session.
   * @param key   -  key of  cache
   * @return cache object or null if nothing found.
   */
  public <T> T get(String group, String key) {

    if (enabled) {
      Object item = cacheManager.getCache(group, key);
      if (item == null) {
        logAccess(group, key, "Miss");
      } else {
        logAccess(group, key, "Hit");
        return (T) item;
      }
    }
    return null;
  }

  static void logAccess(String group, String key, String access) {
    if (logger.isDebugEnabled()) {
      logger.debug(access + ", group: {" + group + "}, key: {" + key + "}");
    }
  }

  public void remove(String group, String key) {
    if (enabled) {
      cacheManager.removeCache(group, key);
    }
  }


  /**
   * This method purges (removes) all caches associated with a table, if caching is enabled and
   * a corresponding model is marked cached.
   *
   * @param group key whose caches are to be purged.
   */
  public void flush(String group) {
    if (enabled) {
      cacheManager.flush(new CacheEvent(group, getClass().getName()));
    }
  }

  public CacheManager getCacheManager() {
    return cacheManager;
  }
}

