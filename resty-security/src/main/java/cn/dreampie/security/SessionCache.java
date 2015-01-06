package cn.dreampie.security;

import java.util.HashMap;

/**
 * Created by wangrenhui on 15/1/6.
 */
public class SessionCache {
  private static final ThreadLocal<SessionCache> current = new ThreadLocal<SessionCache>();



  static class Definition {
    static class Entry<T> {
      private final String key;
      private final CacheLoader<String, T> loader;

      public Entry(String key, CacheLoader<String, T> loader) {
        this.key = key;
        this.loader = loader;
      }
    }

  }
}
