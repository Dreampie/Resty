package cn.dreampie.security;

import cn.dreampie.common.Constant;
import cn.dreampie.security.cache.SessionCache;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static cn.dreampie.common.util.Checker.checkNotNull;

/**
 * Created by ice on 14-12-24.
 */
public class Credentials {

  private final AuthenticateService authenticateService;
  private final long expires;
  private Set<Credential> credentials;
  private Map<String, Principal> principals = new HashMap<String, Principal>();
  private long lastAccess;

  public Credentials(AuthenticateService authenticateService, long expires) {
    this.authenticateService = authenticateService;
    checkNotNull(authenticateService, "Could not find authenticateService to load user.");
    this.expires = expires;
    this.lastAccess = System.currentTimeMillis() + expires;
  }

  /**
   * 获取全部凭据
   *
   * @return 全部凭据
   */
  public Set<Credential> loadAllCredentials() {
    Set<Credential> credentialSet = null;
    if (Constant.cacheEnabled) {
      //load  all  cache
      credentialSet = SessionCache.instance().get(Credential.CREDENTIAL_DEF_KEY, Credential.CREDENTIAL_ALL_KEY);
      if (credentialSet == null) {
        credentialSet = authenticateService.loadAllCredentials();
        SessionCache.instance().add(Credential.CREDENTIAL_DEF_KEY, Credential.CREDENTIAL_ALL_KEY, credentialSet);
      }
    } else {
      if (credentials == null || credentials.size() <= 0 || System.currentTimeMillis() > lastAccess) {
        credentials = authenticateService.loadAllCredentials();
        lastAccess = System.currentTimeMillis() + expires;
      }
      credentialSet = credentials;
    }
    //检测权限数据
    checkNotNull(credentialSet, "Could not get credentials data.");
    return credentialSet;
  }

  /**
   * 获取用户缓存
   *
   * @param username 用户名
   * @return 用户
   */
  public Principal findByUsername(String username) {
    Principal principal = null;
    if (Constant.cacheEnabled) {
      principal = SessionCache.instance().get(Principal.PRINCIPAL_DEF_KEY, username);
      //cache 已经失效  从接口获取用户数据
      if (principal == null) {
        principal = authenticateService.findByUsername(username);
        SessionCache.instance().add(Principal.PRINCIPAL_DEF_KEY, username, principal);
      }
    } else {
      principal = principals.get(username);
      if (principals.size() > 1000 || principals.size() <= 0 || System.currentTimeMillis() > lastAccess) {
        principal = principals.put(username, authenticateService.findByUsername(username));
        lastAccess = System.currentTimeMillis() + expires;
      }
      //如果还没有用户数据
      if (principal == null) {
        principal = principals.put(username, authenticateService.findByUsername(username));
      }
    }
    //检测用户数据
    checkNotNull(principal, "Could not get user data.");
    return principal;
  }
}
