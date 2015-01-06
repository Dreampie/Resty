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

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract method to be sub-classed by various caching technologies.
 */
public abstract class CacheManager {
  private final static Logger logger = Logger.getLogger(CacheManager.class);

  List<CacheEventListener> listeners = new ArrayList<CacheEventListener>();

  /**
   * Returns a cached item. Can return null if not found.
   *
   * @param group group of caches - this is a name of a table for which query results are cached
   * @param key   key of the item.
   * @return a cached item. Can return null if not found.
   */
  public abstract Object getCache(String group, String key);

  /**
   * Adds item to cache.
   *
   * @param group group name of cache.
   * @param key   key of the item.
   * @param cache cache item to add to cache.
   */
  public abstract void addCache(String group, String key, Object cache);


  public abstract void doFlush(CacheEvent event);


  /**
   * Flash cache.
   *
   * @param event type of caches to flush.
   */
  public final void flush(CacheEvent event) {
    doFlush(event);
    for (CacheEventListener listener : listeners) {
      try {
        listener.onFlush(event);
      } catch (Throwable e) {
        logger.warn("failed to propagate cache event: %s to listener: %s", event, listener, e);
      }
    }
    if (logger.isInfoEnabled()) {
      String message = "Cache purged: " + (event.getType() == CacheEvent.CacheEventType.ALL
          ? "all caches" : "table: " + event.getGroup());
      logger.info(message);
    }
  }

  public final void addCacheEventListener(CacheEventListener listener) {
    listeners.add(listener);
  }

  public final void removeCacheEventListener(CacheEventListener listener) {
    listeners.remove(listener);
  }

  public final void removeAllCacheEventListeners() {
    listeners = new ArrayList<CacheEventListener>();
  }
}
