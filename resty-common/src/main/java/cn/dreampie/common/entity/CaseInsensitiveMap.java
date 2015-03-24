package cn.dreampie.common.entity;

import java.util.TreeMap;

/**
 * A case insensitive map for <code>java.lang.String</code> keys. The current implementation is based on
 * {@link java.util.TreeMap}, so it does not accept <code>null</code> keys and keeps entries ordered by case
 * insensitive alphabetical order of keys.
 */
public class CaseInsensitiveMap<V> extends TreeMap<String, V> {

  public CaseInsensitiveMap() {
    super(String.CASE_INSENSITIVE_ORDER);
  }
}
