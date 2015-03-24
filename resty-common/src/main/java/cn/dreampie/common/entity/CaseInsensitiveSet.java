package cn.dreampie.common.entity;

import java.util.TreeSet;

/**
 * A case insensitive map for <code>java.lang.String</code> keys. The current implementation is based on
 * {@link java.util.TreeMap}, so it does not accept <code>null</code> keys and keeps entries ordered by case
 * insensitive alphabetical order of keys.
 */
public class CaseInsensitiveSet extends TreeSet<String> {

  public CaseInsensitiveSet() {
    super(String.CASE_INSENSITIVE_ORDER);
  }
}
