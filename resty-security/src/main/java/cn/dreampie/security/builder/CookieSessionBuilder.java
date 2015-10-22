package cn.dreampie.security.builder;

import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.security.AuthenticateService;
import cn.dreampie.security.SessionBuilder;

/**
 * @author Dreampie
 * @date 2015-10-22
 * @what
 */
public class CookieSessionBuilder extends SessionBuilder {

  public CookieSessionBuilder(AuthenticateService authenticateService) {
    super(authenticateService);
  }

  /**
   * @param cookieName
   * @param authenticateService
   */
  public CookieSessionBuilder(String cookieName, AuthenticateService authenticateService) {
    super(cookieName, authenticateService);
  }

  /**
   * @param limit
   * @param authenticateService
   */

  public CookieSessionBuilder(int limit, AuthenticateService authenticateService) {
    super(limit, authenticateService);
  }

  /**
   * @param limit
   * @param rememberDay
   * @param authenticateService
   */
  public CookieSessionBuilder(int limit, int rememberDay, AuthenticateService authenticateService) {
    super(limit, rememberDay, authenticateService);
  }

  /**
   * @param expires
   * @param limit
   * @param rememberDay
   * @param authenticateService
   */
  public CookieSessionBuilder(long expires, int limit, int rememberDay, AuthenticateService authenticateService) {
    super(expires, limit, rememberDay, authenticateService);
  }

  /**
   * @param cookieName
   * @param expires
   * @param limit
   * @param rememberDay
   * @param authenticateService
   */
  public CookieSessionBuilder(String cookieName, long expires, int limit, int rememberDay, AuthenticateService authenticateService) {
    super(cookieName, expires, limit, rememberDay, authenticateService);
  }

  /**
   * @param request
   * @return
   */
  public String inputSessionKey(HttpRequest request) {
    return request.getCookiesMap().get(sessionName);
  }

  /**
   * @param response
   * @param sessionKey
   * @param expires
   */
  public void outputSessionKey(HttpResponse response, String sessionKey, int expires) {
    response.addCookie(sessionName, sessionKey, expires);
  }
}

