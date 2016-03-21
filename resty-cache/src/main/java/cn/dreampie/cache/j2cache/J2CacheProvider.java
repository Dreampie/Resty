/**
 * Project Name:resty-cache
 * File Name:J2CacheProvider.java
 * Package Name:cn.dreampie.cache.j2cache
 * Date:2016年3月8日下午5:13:51
 * Copyright (c) 2016, 深圳市奔凯安全技术股份有限公司 All Rights Reserved.
 */

package cn.dreampie.cache.j2cache;

import java.util.HashSet;
import java.util.Set;

import cn.dreampie.cache.CacheEvent;
import cn.dreampie.cache.CacheProvider;
import net.oschina.j2cache.CacheChannel;
import net.oschina.j2cache.J2Cache;

/**
 * ClassName:J2CacheProvider <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON. <br/>
 * Date: 2016年3月8日 下午5:13:51 <br/>
 *
 * @author <A href="mailto:shenmt@biocome.com">沈明天</A>
 * @see
 * @since JDK 1.6
 */
public class J2CacheProvider extends CacheProvider {

  static {
    System.setProperty("java.net.preferIPv4Stack", "true"); // Disable IPv6 in JVM
  }

  private static Set<String> groupSet = new HashSet<String>();

  private CacheChannel cache = J2Cache.getChannel();

  public <T> T getCache(String group, String key) {
    return (T) cache.get(group, key).getValue();
  }

  public void addCache(String group, String key, Object cacheObj, int expired) {
    cache.set(group, key, cacheObj);
    if (!groupSet.contains(group)) {
      groupSet.add(group);
    }
  }

  public void removeCache(String group, String key) {
    cache.evict(group, key);
  }

  public void doFlush(CacheEvent event) {
    if (event.getType().equals(CacheEvent.CacheEventType.ALL)) {
      for (String group : groupSet) {
        cache.clear(group);
      }
      groupSet.clear();
    } else if (event.getType().equals(CacheEvent.CacheEventType.GROUP)) {
      cache.clear(event.getGroup());
    }
  }
}
