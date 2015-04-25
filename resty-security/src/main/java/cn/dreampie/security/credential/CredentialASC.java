package cn.dreampie.security.credential;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by wangrenhui on 15/4/25.
 * 升序的Set
 */
public class CredentialASC implements Comparator<Credential>, Serializable {
  public int compare(Credential a, Credential b) {
    int result = a.getAntPath().length() - b.getAntPath().length();
    if (result == 0) {
      result = a.getHttpMethod().compareTo(b.getHttpMethod());
      if (result == 0) {
        return a.getAntPath().compareTo(b.getAntPath());
      }
    }
    return result;
  }
}
