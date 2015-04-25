package cn.dreampie.security;

import cn.dreampie.common.http.exception.WebException;
import cn.dreampie.common.http.result.HttpStatus;
import cn.dreampie.common.util.pattern.AntPathMatcher;
import cn.dreampie.log.Logger;
import cn.dreampie.security.cache.SessionCache;
import cn.dreampie.security.credential.Credential;
import cn.dreampie.security.credential.Credentials;

import java.util.Calendar;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by wangrenhui on 14/12/23.
 */
public class Subject {
  private static final Logger logger = Logger.getLogger(Subject.class);

  private static Credentials credentials;
  private static PasswordService passwordService;
  private static int rememberDay;

  static void init(int rememberDay, Credentials credentials, PasswordService passwordService) {
    Subject.rememberDay = rememberDay;
    Subject.credentials = credentials;
    Subject.passwordService = passwordService;
  }

  public static long getExpires() {
    return Session.current().getExpires();
  }

  public static Principal getPrincipal() {
    return Session.current().getPrincipal();
  }

  public static Map<String, String> getValues() {
    return Session.current().getValues();
  }

  public static void login(String username, String password) {
    login(username, password, false);
  }

  /**
   * login user
   *
   * @param username
   * @param password
   * @param rememberMe
   * @return
   */
  public static void login(String username, String password, boolean rememberMe) {
    Principal principal = credentials.findByUsername(username);
    if (principal != null && passwordService.match(password, principal.getPasswordHash())) {
      //清理已经登陆的对象
      Session.current().clearPrincipal();
      Session.current().set(Session.SESSION_DEF_KEY, null);
      //授权用户
      //时间
      long expires = -1;
      if (rememberMe) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, rememberDay);
        expires = cal.getTimeInMillis();
      }

      Session.current().setExpires(expires);
      String sessionKey = UUID.randomUUID().toString();
      Session.current().authenticateAs(principal);
      Session.current().set(Session.SESSION_DEF_KEY, sessionKey);
      //add cache
      SessionCache.instance().add(Principal.PRINCIPAL_DEF_KEY, username, principal);
      logger.info("Session authentication as " + username);
    } else {
      throw new WebException(HttpStatus.UNAUTHORIZED);
    }
  }

  public static void logout() {
    //add cache
    Principal principal = Session.current().getPrincipal();
    if (principal != null) {
      SessionCache.instance().remove(Principal.PRINCIPAL_DEF_KEY, principal.getUsername());
      logger.info("Session leave authentication " + principal.getUsername());
    }
    Session.current().clearPrincipal();
  }


  public static String get(String key) {
    return Session.current().get(key);
  }

  public static Session set(String key, String value) {
    return Session.current().set(key, value);
  }

  public static String remove(String key) {
    return Session.current().remove(key);
  }


  /**
   * 当前api需要的权限值
   *
   * @param httpMethod httpMethod
   * @param path       path
   * @return value
   */
  public static String need(String httpMethod, String path) {
    Map<String, Map<String, Set<Credential>>> credentialMap = credentials.loadAllCredentials();

    String value;
    if (credentialMap.containsKey(httpMethod)) {
      //匹配method的map
      value = matchPath(httpMethod, path, credentialMap);
      if (value == null) {
        value = matchPath("*", path, credentialMap);
      }
    } else {
      value = matchPath("*", path, credentialMap);
    }
    return value;
  }

  /**
   * 匹配规则，优先httpMethod，其次相同的起始位置
   *
   * @param httpMethod    httpMethod
   * @param path          path
   * @param credentialMap credentialMap
   * @return value
   */
  private static String matchPath(String httpMethod, String path, Map<String, Map<String, Set<Credential>>> credentialMap) {
    if (credentialMap != null && credentialMap.size() > 0) {
      Map<String, Set<Credential>> credentials = credentialMap.get(httpMethod);
      if (credentials.size() > 0) {
        Set<Map.Entry<String, Set<Credential>>> credentialsEntry = credentials.entrySet();
        Set<Credential> credentialSet;
        for (Map.Entry<String, Set<Credential>> credentialEntry : credentialsEntry) {
          if (path.startsWith(credentialEntry.getKey())) {
            credentialSet = credentialEntry.getValue();
            for (Credential credential : credentialSet) {
              if (AntPathMatcher.instance().match(credential.getAntPath(), path)) {
                return credential.getValue();
              }
            }
          }
        }
      }
    }
    return null;
  }

  /**
   * 检测权限
   *
   * @param httpMethod httpMethod
   * @param path       path
   */
  public static void check(String httpMethod, String path) {
    String needCredential = need(httpMethod, path);
    logger.info(httpMethod + " " + path + " need credential " + needCredential);
    if (needCredential != null) {
      Principal principal = Session.current().getPrincipal();
      if (principal != null) {
        if (!principal.hasCredential(needCredential)) {
          throw new WebException(HttpStatus.FORBIDDEN);
        }
      } else {
        throw new WebException(HttpStatus.UNAUTHORIZED);
      }
    }
  }

  /**
   * 判断是否有当前api权限
   *
   * @param httpMethod httpMethod
   * @param path       path
   * @return boolean
   */
  public static boolean has(String httpMethod, String path) {
    String needCredential = need(httpMethod, path);
    if (needCredential != null) {
      Principal principal = Session.current().getPrincipal();
      if (principal != null) {
        if (principal.hasCredential(needCredential)) {
          return true;
        }
      }
    } else {
      return true;
    }
    return false;
  }

}
