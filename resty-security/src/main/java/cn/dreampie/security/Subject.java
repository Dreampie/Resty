package cn.dreampie.security;

import cn.dreampie.common.http.exception.WebException;
import cn.dreampie.common.http.result.HttpStatus;
import cn.dreampie.common.util.matcher.AntPathMatcher;
import cn.dreampie.log.Logger;
import cn.dreampie.security.credential.Credential;
import cn.dreampie.security.credential.Credentials;

import java.util.Calendar;
import java.util.Map;
import java.util.Set;

import static cn.dreampie.common.util.Checker.checkNotNull;

/**
 * Created by wangrenhui on 14/12/23.
 */
public class Subject {
  private static final Logger logger = Logger.getLogger(Subject.class);

  private static final ThreadLocal<Session> sessionTL = new ThreadLocal<Session>();
  private static Credentials credentials;
  private static PasswordService passwordService;
  private static int rememberDay;

  static void init(int rememberDay, Credentials credentials, PasswordService passwordService) {
    Subject.rememberDay = rememberDay;
    Subject.credentials = credentials;
    Subject.passwordService = passwordService;
  }

  static Session current() {
    return sessionTL.get();
  }

  static Session updateCurrent(Session session) {
    if (session != current()) {
      sessionTL.set(session);
    }
    return session;
  }

  private static void removeCurrent() {
    sessionTL.remove();
  }

  private static Session authenticateAs(String username, long expires) {
    Session session = current();
    return updateCurrent(new Session(session.getSessionKey(), username, session.getValues(), expires));
  }

  private static Session clearPrincipal() {
    Session session = current();
    credentials.removePrincipal(current().getUsername());
    return updateCurrent(new Session(session.getSessionKey(), SessionBuilder.ANONYMOUS + "@" + session.get(Sessions.ADDRESS_KEY), session.getValues()));
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
    checkNotNull(username, "Username could not be null.");
    checkNotNull(password, "Password could not be null.");
    Principal principal = credentials.getPrincipal(username);
    if (principal == null) {
      throw new WebException(HttpStatus.NOT_FOUND, "User not found.");
    }
    boolean match;
    String salt = principal.getSalt();
    if (salt != null && !salt.isEmpty()) {
      match = passwordService.match(password, principal.getPassword(), salt);
    } else {
      match = passwordService.match(password, principal.getPassword());
    }

    if (match) {
      //授权用户
      //时间
      long expires = -1;
      if (rememberMe) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, rememberDay);
        expires = cal.getTimeInMillis();
      }
      //授权用户
      authenticateAs(username, expires);
      logger.info("Session authentication as " + username);
    } else {
      throw new WebException(HttpStatus.UNPROCESSABLE_ENTITY, "Password not match.");
    }
  }

  public static void logout() {
    //add cache
    Principal principal = getPrincipal();
    if (principal != null) {
      logger.info("Session leave authentication " + principal.getUsername());
    }
    //清理用户
    clearPrincipal();
  }


  public static void login(String username, String password) {
    login(username, password, false);
  }

  public static long getExpires() {
    return current().getExpires();
  }

  public static Principal getPrincipal() {
    String username = current().getUsername();
    if (username.startsWith(SessionBuilder.ANONYMOUS)) {
      return null;
    } else {
      return credentials.getPrincipal(current().getUsername());
    }
  }

  public static Map<String, String> getValues() {
    return current().getValues();
  }

  public static String get(String key) {
    return current().get(key);
  }

  public static void set(String key, String value) {
    if (!Sessions.ADDRESS_KEY.equals(key) && !Sessions.AGENT_KEY.equals(key)) {
      current().set(key, value);
    }
  }

  public static String remove(String key) {
    if (!Sessions.ADDRESS_KEY.equals(key) && !Sessions.AGENT_KEY.equals(key)) {
      return current().remove(key);
    } else {
      return null;
    }
  }

  public static void refresh() {
    credentials.removePrincipal(current().getUsername());
  }

  /**
   * 当前api需要的权限值
   *
   * @param httpMethod httpMethod
   * @param path       path
   * @return value
   */
  public static String need(String httpMethod, String path) {
    Map<String, Map<String, Set<Credential>>> credentialMap = credentials.getAllCredentials();

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
   * 权限
   *
   * @param httpMethod httpMethod
   * @param path       path
   */
  public static void check(String httpMethod, String path) {
    String needCredential = need(httpMethod, path);
    logger.info(httpMethod + " " + path + " need credential " + needCredential);
    if (needCredential != null) {
      Principal principal = getPrincipal();
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
      Principal principal = getPrincipal();
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

  void set(Map<String, String> values) {
    current().set(values);
  }

}
