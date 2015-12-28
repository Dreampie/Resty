package cn.dreampie.common.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ice on 14-12-29.
 */
public class Maper {
  public static <K, V> Map<K, V> of() {
    return new HashMap<K, V>();
  }

  public static <K, V> Map<K, V> of(final K k1, final V v1) {
    return new HashMap<K, V>() {{
      put(k1, v1);
    }};
  }

  public static <K, V> Map<K, V> of(final K k1, final V v1, final K k2, final V v2) {
    return new HashMap<K, V>() {{
      put(k1, v1);
      put(k2, v2);
    }};
  }

  public static <K, V> Map<K, V> of(final K k1, final V v1, final K k2, final V v2, final K k3, final V v3) {
    return new HashMap<K, V>() {{
      put(k1, v1);
      put(k2, v2);
      put(k3, v3);
    }};
  }

  public static <K, V> Map<K, V> of(final K k1, final V v1, final K k2, final V v2, final K k3, final V v3, final K k4, final V v4) {
    return new HashMap<K, V>() {{
      put(k1, v1);
      put(k2, v2);
      put(k3, v3);
      put(k4, v4);
    }};
  }

  public static <K, V> Map<K, V> of(final K k1, final V v1, final K k2, final V v2, final K k3, final V v3, final K k4, final V v4, final K k5, final V v5) {
    return new HashMap<K, V>() {{
      put(k1, v1);
      put(k2, v2);
      put(k3, v3);
      put(k4, v4);
      put(k5, v5);
    }};
  }

  public static <K, V> Map<K, V> copyOf(Map<K, V> map) {
    return copyOf(new HashMap<K, V>(), map);
  }

  public static <K, V> Map<K, V> copyOf(Map<K, V> dist, Map<K, V> source) {
    dist.putAll(source);
    return dist;
  }
}
