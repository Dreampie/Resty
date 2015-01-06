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
    this.cacheManager = net.sf.ehcache.CacheManager.create(configurationFileName);
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


  public Object getCache(String group, String key) {
    try {
      createIfMissing(group);
      Cache c = cacheManager.getCache(group);
      return c.get(key) == null ? null : c.get(key).getObjectValue();
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


  public void doFlush(CacheEvent event) {

    if (event.getType().equals(CacheEvent.CacheEventType.ALL)) {
      cacheManager.removalAll();
    } else if (event.getType().equals(CacheEvent.CacheEventType.GROUP)) {
      cacheManager.removeCache(event.getGroup());
    }
  }
}
