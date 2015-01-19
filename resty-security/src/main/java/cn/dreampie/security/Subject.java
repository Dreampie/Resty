package cn.dreampie.security;

import cn.dreampie.common.http.HttpStatus;
import cn.dreampie.common.http.exception.WebException;
import cn.dreampie.common.util.pattern.AntPathMatcher;
import cn.dreampie.log.Logger;
import cn.dreampie.security.cache.SessionCache;

import java.util.*;

import static cn.dreampie.common.util.Checker.checkNotNull;

/**
 * Created by wangrenhui on 14/12/23.
 */
public class Subject {
  private static final Logger logger = Logger.getLogger(Subject.class);

  private static AuthenticateService authenticateService;
  private static PasswordService passwordService;
  private static int rememberDay;

  static void init(int rememberDay, AuthenticateService authenticateService, PasswordService passwordService) {
    Subject.rememberDay = rememberDay;
    Subject.authenticateService = authenticateService;
    Subject.passwordService = passwordService;
  }

  public static int getExpires() {
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
    if (authenticateService != null) {
      Principal principal = authenticateService.findByUsername(username);
      if (principal != null && passwordService.match(password, principal.getPasswordHash())) {
        //清理已经登陆的对象
        Session.current().clearPrincipal();
        Session.current().set(Session.SESSION_DEF_KEY, null);
        //授权用户
        //时间
        int expires = -1;
        if (rememberMe) {
          Calendar cal = Calendar.getInstance();
          cal.setTime(new Date());
          cal.add(Calendar.DATE, rememberDay);
          expires = (int) cal.getTimeInMillis();
        }
        Session.current().setExpires(expires);
        String sessionKey = UUID.randomUUID().toString();
        Session.current().authenticateAs(principal);
        Session.current().set(Session.SESSION_DEF_KEY, sessionKey);
        //add cache
        SessionCache.instance().add(Principal.PRINCIPAL_DEF_KEY, username, principal);
        logger.info("Session authentication as " + username);
      } else
        throw new WebException(HttpStatus.UNAUTHORIZED);
    } else {
      throw new WebException(HttpStatus.UNAUTHORIZED, "AuthenticateService not found!");
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
    Session.current().set(Session.SESSION_DEF_KEY, null);
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
   * 当前的路径需要的权限值
   *
   * @param httpMethod
   * @param path
   * @return
   */
  public static String need(String httpMethod, String path) {
    Set<Credential> permissions = SessionCache.instance().get(Credential.CREDENTIAL_DEF_KEY, Credential.CREDENTIAL_ALL_KEY);
    if (permissions == null) {
      permissions = authenticateService.loadAllCredentials();
    }
    checkNotNull(permissions, "LoadAllPermissions not get permissions data.");
    String method;
    for (Credential permission : permissions) {
      method = permission.getMethod();
      if ((method.equals("*") || method.equals(httpMethod))
          && AntPathMatcher.instance().match(permission.getAntPath(), path)) {
        return permission.getValue();
      }
    }
    return null;
  }

  /**
   * 检测权限
   *
   * @param httpMethod
   * @param path
   */
  public static void check(String httpMethod, String path) {
    String needPermisssion = need(httpMethod, path);
    if (needPermisssion != null) {
      Principal principal = Session.current().getPrincipal();
      if (principal != null) {
        if (!principal.hasPermission(needPermisssion)) {
          throw new WebException(HttpStatus.FORBIDDEN);
        }
      } else {
        throw new WebException(HttpStatus.UNAUTHORIZED);
      }
    }
  }

  /**
   * 判断是否用权限
   *
   * @param httpMethod
   * @param path
   * @return
   */
  public static boolean has(String httpMethod, String path) {
    String needPermisssion = need(httpMethod, path);
    if (needPermisssion != null) {
      Principal principal = Session.current().getPrincipal();
      if (principal != null) {
        if (principal.hasPermission(needPermisssion)) {
          return true;
        }
      }
    } else {
      return true;
    }
    return false;
  }

}
