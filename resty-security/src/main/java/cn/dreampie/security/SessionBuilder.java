package cn.dreampie.security;


import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.common.util.Maper;
import cn.dreampie.common.util.json.Jsoner;
import cn.dreampie.log.Logger;
import cn.dreampie.security.credential.Credentials;
import cn.dreampie.security.sign.CookieSigner;
import cn.dreampie.security.sign.Signer;

import java.util.Map;

/**
 * Created by ice on 14-12-24.
 */
public class SessionBuilder {

  private final static Logger logger = Logger.getLogger(SessionBuilder.class);
  private final static String ANONYMOUS = "anonymous";
  private final Sessions sessions;
  private final Signer signer;
  private final SessionCookieDescriptor sessionCookieDescriptor;
  private final Session emptySession;

  public SessionBuilder(long defaultExpires, int limit, int rememberDay, AuthenticateService authenticateService) {
    this.sessions = new Sessions(defaultExpires, limit);
    this.signer = new CookieSigner();
    this.sessionCookieDescriptor = new SessionCookieDescriptor();
    this.emptySession = new Session();

    Subject.init(rememberDay, new Credentials(authenticateService, defaultExpires), authenticateService.getPasswordService());
  }

  /**
   * 从cookie读取用户信息
   *
   * @param request
   * @return
   */
  public Session in(HttpRequest request) {
    Session session = buildSession(request);
    Subject.updateCurrent(session);
    return session;
  }

  /**
   * 写出到cookie
   *
   * @param oldSession
   * @param response
   */
  public Session out(Session oldSession, HttpResponse response) {
    Session session = Subject.current();
    String username = null;
    String sessionKey = session.getSessionKey();
    Principal principal = session.getPrincipal();

    if (principal != null) {
      username = principal.getUsername();
    } else {
      username = getAnonymousName(session);
    }
    if (session != oldSession) {
      //更新cookie
      updateCookie(response, session);
      //重新存储session数据
      String oldSessionKey = oldSession.getSessionKey();
      String oldUsername = null;
      Principal oldPrincipal = oldSession.getPrincipal();
      //原本的session
      if (oldPrincipal != null) {
        oldUsername = oldPrincipal.getUsername();
      } else {
        oldUsername = getAnonymousName(oldSession);
      }
      //现在的session
      sessions.update(oldUsername, oldSessionKey, username, sessionKey, session);
    } else {
      sessions.update(username, sessionKey, session);
    }
    return session;
  }

  /**
   * 构建session数据
   *
   * @param request
   * @return
   */
  private Session getSession(HttpRequest request, Session session) {
    String agent = request.getHeader("User-Agent");
    session.set(Maper.of(Sessions.ADDRESS_KEY, request.getClientAddress(),
        Sessions.AGENT_KEY, agent == null ? "Unknown" : agent));
    return session;
  }

  /**
   * 获取匿名用户的名字
   *
   * @param session
   * @return
   */
  private String getAnonymousName(Session session) {
    return ANONYMOUS + "@" + session.get(Sessions.ADDRESS_KEY);
  }

  /**
   * 初始化session
   *
   * @param request
   * @return
   */
  private Session buildSession(HttpRequest request) {
    String sessionCookieName = sessionCookieDescriptor.getCookieName();
    String cookie = request.getCookieValue(sessionCookieName);
    if (cookie != null) {
      logger.debug("Session cookie was: %s.", cookie);
      String sig = request.getCookieValue(sessionCookieDescriptor.getCookieSignatureName());
      logger.debug("Session signature was: %s.", sig);
      if (sig != null && signer.verify(cookie, sig)) {
        Map<String, String> cookies = readCookies(cookie);
        //失效时间
        Map<String, String> cookieValues = Maper.copyOf(cookies);
        String principalName = cookieValues.get(Principal.PRINCIPAL_DEF_KEY);
        if (principalName != null && !"".equals(principalName.trim())) {
          //判断 是否使用了 remeberme 或失效
          Sessions.SessionDatas sessionDatas = sessions.get(principalName);
          if (sessionDatas != null) {
            String sessionKey = cookieValues.get(Session.SESSION_DEF_KEY);
            Sessions.SessionData sessionData = sessionDatas.getSessionData(sessionKey);
            if (sessionData != null) {
              return sessionData.getSession();
            }
          } else {
            logger.warn("Invalid user session. session was: %s. Ignoring session cookie.", cookie);
          }
        } else {
          logger.warn("Invalid username. session was: %s. Ignoring session cookie.", cookie);
        }
      } else {
        logger.warn("Invalid session signature. session was: %s. Ignoring session cookie.", cookie);
      }
    }
    return getSession(request, emptySession);
  }

  /**
   * 转换数据cookie
   *
   * @param cookie
   * @return
   */
  private Map<String, String> readCookies(String cookie) {
    return (Map<String, String>) Jsoner.toObject(cookie, Map.class);
  }

  /**
   * 写入cookie到客户端
   *
   * @param resp
   * @param session
   */
  private void updateCookie(HttpResponse resp, Session session) {
    Map<String, String> cookiesMap = getCookiesMap(session);
    if (cookiesMap.isEmpty()) {
      resp.clearCookie(sessionCookieDescriptor.getCookieName());
      resp.clearCookie(sessionCookieDescriptor.getCookieSignatureName());
    } else {
      for (Map.Entry<String, String> cookie : cookiesMap.entrySet()) {
        resp.addCookie(cookie.getKey(), cookie.getValue(), session.getExpires() > 0 ? (int) (session.getExpires() / 1000) : (int) session.getExpires());
      }
    }
  }

  /**
   * session 转换为cookie
   *
   * @param session
   * @return
   */
  private Map<String, String> getCookiesMap(Session session) {

    Map<String, String> sessionMap = Maper.of(Session.SESSION_DEF_KEY, session.getSessionKey());
    Principal principal = session.getPrincipal();
    if (principal != null) {
      sessionMap.put(Principal.PRINCIPAL_DEF_KEY, principal.getUsername());
    } else {
      sessionMap.put(Principal.PRINCIPAL_DEF_KEY, getAnonymousName(session));
    }
    String sessionJson = Jsoner.toJSON(sessionMap);
    return Maper.of(sessionCookieDescriptor.getCookieName(), sessionJson,
        sessionCookieDescriptor.getCookieSignatureName(), signer.sign(sessionJson));
  }
}
