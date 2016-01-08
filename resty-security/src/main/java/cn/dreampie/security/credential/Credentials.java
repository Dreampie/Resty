package cn.dreampie.security.credential;

import cn.dreampie.common.Constant;
import cn.dreampie.common.entity.CaseInsensitiveMap;
import cn.dreampie.security.AuthenticateService;
import cn.dreampie.security.Principal;
import cn.dreampie.cache.SimpleCache;

import java.util.*;

import static cn.dreampie.common.util.Checker.checkNotNull;

/**
 * Created by ice on 14-12-24.
 */
public class Credentials {

  private final AuthenticateService authenticateService;
  private final long expires;
  private Map<String, Map<String, Set<Credential>>> credentialMap = new CaseInsensitiveMap<Map<String, Set<Credential>>>();
  private Map<String, Principal> principals = new HashMap<String, Principal>();
  private long lastAccess;

  public Credentials(AuthenticateService authenticateService, long expires) {
    this.authenticateService = authenticateService;
    checkNotNull(authenticateService, "Could not find authenticateService to load user.");
    this.expires = expires;
    this.lastAccess = System.currentTimeMillis() + expires;
  }

  /**
   * 加倒认证map
   *
   * @param credentialSet 认证set
   */
  private Map<String, Map<String, Set<Credential>>> addCredentials(Set<Credential> credentialSet) {
    Map<String, Map<String, Set<Credential>>> credentialMap = new CaseInsensitiveMap<Map<String, Set<Credential>>>();

    Map<String, Set<Credential>> credentials;
    Set<Map.Entry<String, Set<Credential>>> credentialsEntrySet;
    String httpMethod;
    String antPath;
    int sIndex = -1;
    String antPathKey;
    boolean wasAdd = false;


    Set<Credential> credentialDESCSet;
    Map<String, Set<Credential>> credentialDESCMap;
    for (Credential credential : credentialSet) {
      httpMethod = credential.getHttpMethod();
      antPath = credential.getAntPath();

      sIndex = antPath.indexOf('*');
      if (sIndex > 0) {
        if (antPath.charAt(sIndex - 1) == '/') {
          antPathKey = antPath.substring(0, sIndex - 1);
        } else {
          antPathKey = antPath.substring(0, sIndex);
        }
      } else {
        antPathKey = antPath;
      }

      if (credentialMap.containsKey(httpMethod)) {
        //通过httpMethod来获取认证Map
        credentials = credentialMap.get(httpMethod);
        credentialsEntrySet = credentials.entrySet();
        for (Map.Entry<String, Set<Credential>> credentialsEntry : credentialsEntrySet) {
          //如果有相同的前半部分
          if (antPath.startsWith(credentialsEntry.getKey())) {
            credentialsEntry.getValue().add(credential);
            wasAdd = true;
            break;
          }
        }
        //如果没有找到相同前缀的
        if (!wasAdd) {
          if (credentials.containsKey(antPathKey)) {
            credentials.get(antPathKey).add(credential);
          } else {
            credentialDESCSet = new TreeSet<Credential>(new CredentialASC());
            credentialDESCSet.add(credential);
            credentials.put(antPathKey, credentialDESCSet);
          }
        }

      } else {
        credentialDESCSet = new TreeSet<Credential>(new CredentialASC());
        credentialDESCSet.add(credential);
        credentialDESCMap = new TreeMap<String, Set<Credential>>(new CredentialKeyDESC());
        credentialDESCMap.put(antPathKey, credentialDESCSet);
        credentialMap.put(httpMethod, credentialDESCMap);
      }
    }
    return credentialMap;
  }


  /**
   * 取全部凭据
   *
   * @return 全部凭据
   */
  public Map<String, Map<String, Set<Credential>>> getAllCredentials() {
    if (Constant.cacheEnabled) {
      //load  all  cache
      credentialMap = SimpleCache.instance().get(Credential.CREDENTIAL_DEF_KEY, Credential.CREDENTIAL_ALL_KEY);
      if (credentialMap == null) {
        credentialMap = addCredentials(authenticateService.getAllCredentials());
        SimpleCache.instance().add(Credential.CREDENTIAL_DEF_KEY, Credential.CREDENTIAL_ALL_KEY, credentialMap);
      }
    } else {
      if (credentialMap.size() <= 0 || System.currentTimeMillis() > lastAccess) {
        Set<Credential> credentialASCSet = new TreeSet<Credential>(new CredentialASC());
        credentialASCSet.addAll(authenticateService.getAllCredentials());
        credentialMap = addCredentials(credentialASCSet);
        lastAccess = System.currentTimeMillis() + expires;
      }
    }
    //检测权限数据
    checkNotNull(credentialMap, "Could not get credentials data.");
    return credentialMap;
  }

  /**
   * 取用户缓存
   *
   * @param username 用户名
   * @return 用户
   */
  public Principal getPrincipal(String username) {
    Principal principal;
    if (Constant.cacheEnabled) {
      principal = SimpleCache.instance().get(Principal.PRINCIPAL_DEF_KEY, username);
      //cache 已经失效  从接口获取用户数据
      if (principal == null) {
        principal = authenticateService.getPrincipal(username);
        SimpleCache.instance().add(Principal.PRINCIPAL_DEF_KEY, username, principal, (int) expires);
      }
    } else {
      boolean find = false;
      if (principals.size() > 1000 || principals.size() <= 0 || System.currentTimeMillis() > lastAccess) {
        principal = authenticateService.getPrincipal(username);
        principals.put(username, principal);
        lastAccess = System.currentTimeMillis() + expires;
        find = true;
      } else {
        principal = principals.get(username);
      }
      //如果还没有用户数据
      if (!find && principal == null) {
        principal = authenticateService.getPrincipal(username);
        principals.put(username, principal);
      }
    }
    return principal;
  }

  /**
   * 删除用户缓存
   *
   * @param username
   */
  public void removePrincipal(String username) {
    if (Constant.cacheEnabled) {
      SimpleCache.instance().remove(Principal.PRINCIPAL_DEF_KEY, username);
    } else {
      principals.remove(username);
    }
  }
}
