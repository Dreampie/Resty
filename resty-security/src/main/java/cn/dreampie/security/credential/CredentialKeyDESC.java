package cn.dreampie.security.credential;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by wangrenhui on 15/4/25.
 * 创建一个对key排序的map
 */
public class CredentialKeyDESC implements Comparator<String>, Serializable {
  public int compare(String k1, String k2) {
    int result = k2.length() - k1.length();
    if (result == 0) {
      return k1.compareTo(k2);
    }
    return result;
  }
}
