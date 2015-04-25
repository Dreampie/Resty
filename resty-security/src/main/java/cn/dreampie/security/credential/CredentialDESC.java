package cn.dreampie.security.credential;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by wangrenhui on 15/4/25.
 * 创建一个倒排序的认证的Set
 */
public class CredentialDESC implements Comparator<Credential>, Serializable {
  public int compare(Credential a, Credential b) {
    int result = b.getAntPath().length() - a.getAntPath().length();
    if (result == 0) {
      result = a.getHttpMethod().compareTo(b.getHttpMethod());
      if (result == 0) {
        return a.getAntPath().compareTo(b.getAntPath());
      }
    }
    return result;
  }
}
