package cn.dreampie.security.cache;


import cn.dreampie.cache.CacheEvent;
import cn.dreampie.cache.CacheProvider;
import cn.dreampie.common.Constant;
import cn.dreampie.log.Logger;


/**
 * This is a main cache facade. It could be architected in the future to add more cache implementations besides OSCache.
 */
public enum SessionCache {
  INSTANCE;

  private final static Logger logger = Logger.getLogger(SessionCache.class);

  private final boolean enabled = Constant.cacheEnabled;

  private final CacheProvider cacheProvider;

  //singleton

  private SessionCache() {
    cacheProvider = CacheProvider.PROVIDER;
  }


  /**
   * This class is a singleton, get an instance with this method.
   *
   * @return one and only one instance of this class.
   */
  public static SessionCache instance() {
    return INSTANCE;
  }

  static void logAccess(String group, String key, String access) {
    if (logger.isDebugEnabled()) {
      logger.debug(access + ", group: {" + group + "}, key: {" + key + "}");
    }
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
      cacheProvider.addCache(group, key, value);
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
      Object item = cacheProvider.getCache(group, key);
      if (item == null) {
        logAccess(group, key, "Miss");
      } else {
        logAccess(group, key, "Hit");
        return (T) item;
      }
    }
    return null;
  }

  public void remove(String group, String key) {
    if (enabled) {
      cacheProvider.removeCache(group, key);
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
      cacheProvider.flush(new CacheEvent(group, getClass().getName()));
    }
  }

  public CacheProvider getCacheProvider() {
    return cacheProvider;
  }
}

