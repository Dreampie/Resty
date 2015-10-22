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
public class BothSessionBuilder extends SessionBuilder {

  public BothSessionBuilder(AuthenticateService authenticateService) {
    super(authenticateService);
  }

  public BothSessionBuilder(String cookieName, AuthenticateService authenticateService) {
    super(cookieName, authenticateService);
  }

  public BothSessionBuilder(int limit, AuthenticateService authenticateService) {
    super(limit, authenticateService);
  }

  public BothSessionBuilder(int limit, int rememberDay, AuthenticateService authenticateService) {
    super(limit, rememberDay, authenticateService);
  }

  public BothSessionBuilder(long expires, int limit, int rememberDay, AuthenticateService authenticateService) {
    super(expires, limit, rememberDay, authenticateService);
  }

  public BothSessionBuilder(String cookieName, long expires, int limit, int rememberDay, AuthenticateService authenticateService) {
    super(cookieName, expires, limit, rememberDay, authenticateService);
  }

  public String inputSessionKey(HttpRequest request) {
    String sessionKey = request.getCookiesMap().get(sessionName);
    if (sessionKey == null) {
      sessionKey = request.getHeader(sessionName);
    }
    return sessionKey;
  }

  public void outputSessionKey(HttpResponse response, String sessionKey, int expires) {
    response.addHeader(sessionName, sessionKey);
    response.addCookie(sessionName, sessionKey, expires);
  }
}

