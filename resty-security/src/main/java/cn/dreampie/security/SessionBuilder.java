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
import java.util.UUID;

import static cn.dreampie.common.util.Checker.checkNotNull;

/**
 * Created by ice on 14-12-24.
 */
public class SessionBuilder {

  private static final String EXPIRES = "_expires";

  private final static Logger logger = Logger.getLogger(SessionBuilder.class);

  private final Sessions sessions;
  private final Signer signer;
  private final SessionCookieDescriptor sessionCookieDescriptor;
  private final Session emptySession;
  private final Credentials credentials;
  private final int expires;

  public SessionBuilder(int expires, int limit, int rememberDay, AuthenticateService authenticateService) {
    this(expires, limit, rememberDay, authenticateService, new DefaultPasswordService());
  }

  public SessionBuilder(int expires, int limit, int rememberDay, AuthenticateService authenticateService, PasswordService passwordService) {
    this.expires = expires;
    this.sessions = new Sessions(expires, limit);
    this.signer = new CookieSigner();
    this.sessionCookieDescriptor = new SessionCookieDescriptor();
    this.emptySession = new Session(Maper.of(Session.SESSION_DEF_KEY, UUID.randomUUID().toString()), null, -1);
    this.credentials = new Credentials(authenticateService, expires);

    Subject.init(rememberDay, credentials, passwordService);
  }

  /**
   * 从cookie读取用户信息
   *
   * @param request
   * @return
   */
  public Session in(HttpRequest request) {
    Session session = buildSession(request);
    Session.setCurrent(session);
    return session;
  }

  /**
   * 写出到cookie
   *
   * @param session
   * @param response
   */
  public Session out(Session session, HttpRequest request, HttpResponse response) {
    Session newSession = Session.current();
    if (newSession != session) {
      //更新cookie
      updateCookie(response, newSession);
      //重新存储session数据
      Map<String, String> metadata = buildSessionMetadata(request);
      String oldSessionKey = session.get(Session.SESSION_DEF_KEY);
      String sessionKey = newSession.get(Session.SESSION_DEF_KEY);
      String oldName = null;
      String name = null;
      Principal oldPrincipal = session.getPrincipal();
      Principal principal = newSession.getPrincipal();
      //原本的session
      if (oldPrincipal != null) {
        oldName = oldPrincipal.getUsername();
      } else {
        oldName = "anonymous@" + request.getClientAddress();
      }
      //现在的session
      if (principal != null) {
        name = principal.getUsername();
        sessions.touch(oldName, oldSessionKey, name, sessionKey, metadata, newSession.getExpires());
      } else {
        name = "anonymous@" + request.getClientAddress();
        sessions.touch(oldName, oldSessionKey, name, sessionKey, metadata, newSession.getExpires());
      }
    }
    return newSession;
  }

  /**
   * 构建session数据
   *
   * @param request
   * @return
   */
  private Map<String, String> buildSessionMetadata(HttpRequest request) {
    String agent = request.getHeader("User-Agent");
    return Maper.of(
        Sessions.ADDRESS_KEY, request.getClientAddress(),
        Sessions.AGENT_KEY, agent == null ? "Unknown" : agent);
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
    if (cookie == null || cookie.trim().isEmpty()) {
      return emptySession;
    } else {
      String sig = request.getCookieValue(sessionCookieDescriptor.getCookieSignatureName());
      if (sig == null || !signer.verify(cookie, sig)) {
        logger.warn("Invalid session signature. session was: %s. Ignoring session cookie.", cookie);
        return emptySession;
      }
      Map<String, String> entries = readEntries(cookie);
      String expiresCookie = entries.remove(EXPIRES);
      //失效时间
      if (expiresCookie != null && !"".equals(expiresCookie.trim())) {
        long expiration = -1;
        if (!"-1".equals(expiresCookie)) {
          long expires = Long.parseLong(expiresCookie);
          if (expires > System.currentTimeMillis() + this.expires)
            expiration = expires;
        }
        Map<String, String> cookieValues = Maper.copyOf(entries);
        String principalName = cookieValues.get(Principal.PRINCIPAL_DEF_KEY);
        Principal principal = null;
        if (principalName != null && !"".equals(principalName.trim())) {
          //判断 是否使用了 remeberme 或失效
          Sessions.SessionDatas sessionDatas = sessions.getSessions(principalName);
          if (sessionDatas == null || (!sessionDatas.containsSessionKey(cookieValues.get(Session.SESSION_DEF_KEY)))) {
            return emptySession;
          }
          principal = credentials.findByUsername(principalName);
          //检测用户数据
          checkNotNull(principal, "Could not get user data.");
        }
        return new Session(cookieValues, principal, expiration);
      } else {
        return emptySession;
      }
    }
  }

  /**
   * 转换数据cookie
   *
   * @param cookie
   * @return
   */
  private Map<String, String> readEntries(String cookie) {
    return (Map<String, String>) Jsoner.toObject(cookie, Map.class);
  }

  /**
   * 写入cookie到客户端
   *
   * @param resp
   * @param session
   */
  private void updateCookie(HttpResponse resp, Session session) {
    Map<String, String> cookiesMap = toCookiesMap(session);
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
  private Map<String, String> toCookiesMap(Session session) {
    Map<String, String> sessionMap = session.getValues();
    if (sessionMap.isEmpty()) {
      return Maper.of();
    } else {
      Map<String, String> map = Maper.copyOf(sessionMap);
      map.put(EXPIRES, Long.toString(session.getExpires()));
      String sessionJson = Jsoner.toJSON(map);
      return Maper.of(sessionCookieDescriptor.getCookieName(), sessionJson,
          sessionCookieDescriptor.getCookieSignatureName(), signer.sign(sessionJson));
    }
  }
}
