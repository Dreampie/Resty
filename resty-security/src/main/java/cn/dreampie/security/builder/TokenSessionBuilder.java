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
public class TokenSessionBuilder extends SessionBuilder {

  public TokenSessionBuilder(AuthenticateService authenticateService) {
    super(authenticateService);
  }

  public TokenSessionBuilder(String cookieName, AuthenticateService authenticateService) {
    super(cookieName, authenticateService);
  }

  public TokenSessionBuilder(int limit, AuthenticateService authenticateService) {
    super(limit, authenticateService);
  }

  public TokenSessionBuilder(int limit, int rememberDay, AuthenticateService authenticateService) {
    super(limit, rememberDay, authenticateService);
  }

  public TokenSessionBuilder(long expires, int limit, int rememberDay, AuthenticateService authenticateService) {
    super(expires, limit, rememberDay, authenticateService);
  }

  public TokenSessionBuilder(String cookieName, long expires, int limit, int rememberDay, AuthenticateService authenticateService) {
    super(cookieName, expires, limit, rememberDay, authenticateService);
  }

  public String inputSessionKey(HttpRequest request) {
    return request.getHeader(sessionName);
  }

  public void outputSessionKey(HttpResponse response, String sessionKey, int expires) {
    response.addHeader(sessionName, sessionKey);
  }
}

