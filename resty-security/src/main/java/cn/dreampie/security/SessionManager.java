package cn.dreampie.security;

import cn.dreampie.security.cache.SessionCache;

/**
 * Created by ice on 15-1-7.
 */
public class SessionManager {


  public static Principal getPrincipal(String name) {
    Principal principal = SessionCache.instance().get(Principal.PRINCIPAL_DEF_KEY, name);

    if (principal == null) {

    }
    return principal;
  }

  public static void addPrincipal(Principal principal) {
    SessionCache.instance().add(Principal.PRINCIPAL_DEF_KEY, principal.getUsername(), principal);
  }


}
