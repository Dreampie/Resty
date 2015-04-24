package cn.dreampie.cache.ehcache;

import cn.dreampie.cache.CacheEvent;
import cn.dreampie.cache.CacheManager;
import cn.dreampie.log.Logger;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.Configuration;

import java.io.InputStream;
import java.net.URL;

/**
 * EHCacheManager
 */
public class EHCacheManager extends CacheManager {
  private static final Logger logger = Logger.getLogger(EHCacheManager.class);
  private final net.sf.ehcache.CacheManager cacheManager;

  public EHCacheManager() {
    cacheManager = net.sf.ehcache.CacheManager.create();
  }

  public EHCacheManager(net.sf.ehcache.CacheManager cacheManager) {
    this.cacheManager = cacheManager;
  }

  public EHCacheManager(String configurationFileName) {
    this(EHCacheManager.class.getResourceAsStream(configurationFileName));
  }

  public EHCacheManager(URL configurationFileURL) {
    this.cacheManager = net.sf.ehcache.CacheManager.create(configurationFileURL);
  }

  public EHCacheManager(InputStream inputStream) {
    this.cacheManager = net.sf.ehcache.CacheManager.create(inputStream);
  }

  public EHCacheManager(Configuration configuration) {
    this.cacheManager = net.sf.ehcache.CacheManager.create(configuration);
  }


  public <T> T getCache(String group, String key) {
    try {
      createIfMissing(group);
      Cache c = cacheManager.getCache(group);
      return (T) (c.get(key) == null ? null : c.get(key).getObjectValue());
    } catch (Exception e) {
      logger.warn("%s", e, e);
      return null;
    }
  }

  private void createIfMissing(String group) {
    //double-checked synchronization is broken in Java, but this should work just fine.
    if (cacheManager.getCache(group) == null) {
      try {
        cacheManager.addCache(group);
      } catch (net.sf.ehcache.ObjectExistsException ignore) {
        logger.warn(ignore.getMessage());
      }
    }
  }


  public void addCache(String group, String key, Object cache) {
    createIfMissing(group);
    cacheManager.getCache(group).put(new Element(key, cache));
  }

  public void removeCache(String group, String key) {
    if (cacheManager.getCache(group) != null) {
      cacheManager.getCache(group).remove(key);
    }
  }

  public void doFlush(CacheEvent event) {

    if (event.getType().equals(CacheEvent.CacheEventType.ALL)) {
      cacheManager.removalAll();
    } else if (event.getType().equals(CacheEvent.CacheEventType.GROUP)) {
      cacheManager.removeCache(event.getGroup());
    }
  }
}
