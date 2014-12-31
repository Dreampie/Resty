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
import cn.dreampie.log.LoggerFactory;
import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;

import java.util.Properties;

/**
 * OSCacheManager
 */
public class OSCacheManager extends CacheManager {
  private static final Logger logger = LoggerFactory.getLogger(EHCacheManager.class);
  private final GeneralCacheAdministrator administrator;

  public OSCacheManager() {
    administrator = new GeneralCacheAdministrator();
  }

  public OSCacheManager(Properties properties) {
    administrator = new GeneralCacheAdministrator(properties);
  }


  public Object getCache(String group, String key) {
    try {
      return administrator.getFromCache(key);
    } catch (NeedsRefreshException nre) {
      try {
        administrator.cancelUpdate(key);
      } catch (Exception e) {
        logger.warn(e.getMessage());
        return null;
      }

    }
    return null;
  }


  public void addCache(String group, String key, Object cache) {
    try {
      administrator.putInCache(key, cache, new String[]{group});
    } catch (Exception ignore) {
      logger.warn(ignore.getMessage());
    }
  }


  public void doFlush(CacheEvent event) {

    if (event.getType().equals(CacheEvent.CacheEventType.ALL)) {
      administrator.flushAll();
    } else if (event.getType().equals(CacheEvent.CacheEventType.GROUP)) {
      administrator.flushGroup(event.getGroup());
    }
  }
}
