package cn.dreampie.security;

import cn.dreampie.common.http.HttpStatus;
import cn.dreampie.common.http.exception.WebException;
import cn.dreampie.common.util.pattern.AntPathMatcher;
import cn.dreampie.security.cache.SessionCache;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static cn.dreampie.common.util.Checker.checkNotNull;

/**
 * Created by wangrenhui on 14/12/23.
 */
public class Subject {
  private static AuthenticateService authenticateService;
  private static PasswordService passwordService;
  private static int rememberDay;

  static void init(int rememberDay, AuthenticateService authenticateService, PasswordService passwordService) {
    Subject.rememberDay = rememberDay;
    Subject.authenticateService = authenticateService;
    Subject.passwordService = passwordService;
  }

  public static Session current() {
    return Session.current();
  }

  public static Session login(String username, String password) {
    return login(username, password, false);
  }

  /**
   * login user
   *
   * @param username
   * @param password
   * @param rememberMe
   * @return
   */
  public static Session login(String username, String password, boolean rememberMe) {
    if (authenticateService != null) {
      Principal principal = authenticateService.findByName(username);
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
        return Session.current();
      } else
        throw new WebException(HttpStatus.UNAUTHORIZED);
    } else {
      throw new WebException(HttpStatus.UNAUTHORIZED, "AuthenticateService not found!");
    }
  }

  public static void logout() {
    //add cache
    Principal principal = Session.current().getPrincipal();
    if (principal != null)
      SessionCache.instance().remove(Principal.PRINCIPAL_DEF_KEY, principal.getUsername());
    Session.current().clearPrincipal();
    Session.current().set(Session.SESSION_DEF_KEY, null);
  }

  /**
   * 当前的路径需要的权限值
   *
   * @param httpMethod
   * @param path
   * @return
   */
  public static String need(String httpMethod, String path) {
    Set<Permission> permissions = SessionCache.instance().get(Permission.PERMISSION_DEF_KEY, Permission.PERMISSION_ALL_KEY);
    if (permissions == null) {
      permissions = authenticateService.loadAllPermissions();
    }
    checkNotNull(permissions, "LoadAllPermissions not get permissions data.");
    for (Permission permission : permissions) {
      if (permission.getMethod().equals(httpMethod)
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
