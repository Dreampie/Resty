package cn.dreampie.security.credential;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by wangrenhui on 15/4/25.
 * 创建一个对key排序的map
 */
public class CredentialDESCMap extends TreeMap<String, Set<Credential>> implements Comparator<String>, Serializable {
  public int compare(String k1, String k2) {
    int result = k2.length() - k1.length();
    if (result == 0) {
      return k1.compareTo(k2);
    }
    return result;
  }
}
