package cn.dreampie.cache.ehcache;

import cn.dreampie.cache.CacheEvent;
import cn.dreampie.cache.CacheProvider;
import cn.dreampie.log.Logger;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.Configuration;

import java.io.InputStream;
import java.net.URL;

/**
 * EHCacheProvider
 */
public class EHCacheProvider extends CacheProvider {
  private static final Logger logger = Logger.getLogger(EHCacheProvider.class);
  private final net.sf.ehcache.CacheManager cacheManager;

  public EHCacheProvider() {
    cacheManager = net.sf.ehcache.CacheManager.create();
  }

  public EHCacheProvider(net.sf.ehcache.CacheManager cacheManager) {
    this.cacheManager = cacheManager;
  }

  public EHCacheProvider(String configurationFileName) {
    this(EHCacheProvider.class.getResourceAsStream(configurationFileName));
  }

  public EHCacheProvider(URL configurationFileURL) {
    this.cacheManager = net.sf.ehcache.CacheManager.create(configurationFileURL);
  }

  public EHCacheProvider(InputStream inputStream) {
    this.cacheManager = net.sf.ehcache.CacheManager.create(inputStream);
  }

  public EHCacheProvider(Configuration configuration) {
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
      } catch (Exception ignore) {
        logger.warn(ignore.getMessage());
      }
    }
  }

  public void addCache(String group, String key, Object cache, int expired) {
    createIfMissing(group);
    Element element;
    if (expired != -1) {
      element = new Element(key, cache, false, expired, expired);
    } else {
      element = new Element(key, cache);
    }
    cacheManager.getCache(group).put(element);
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
