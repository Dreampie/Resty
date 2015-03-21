package cn.dreampie.security;


import cn.dreampie.common.Constant;
import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.common.util.Maper;
import cn.dreampie.common.util.json.Jsoner;
import cn.dreampie.log.Logger;
import cn.dreampie.security.cache.SessionCache;
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

  private final boolean cacheEnabled = Constant.cacheEnabled;
  private final Sessions sessions;
  private final Signer signer;
  private final SessionCookieDescriptor sessionCookieDescriptor;
  private final Session emptySession;
  private final AuthenticateService authenticateService;
  private final int expires;

  public SessionBuilder(int expires, int limit, int rememberDay, AuthenticateService authenticateService) {
    this(expires, limit, rememberDay, authenticateService, new DefaultPasswordService());
  }

  public SessionBuilder(int expires, int limit, int rememberDay, AuthenticateService authenticateService, PasswordService passwordService) {
    Subject.init(rememberDay, authenticateService, passwordService);
    this.expires = expires;
    this.sessions = new Sessions(expires, limit);
    this.signer = new CookieSigner();
    this.sessionCookieDescriptor = new SessionCookieDescriptor();
    this.emptySession = new Session(Maper.of(Session.SESSION_DEF_KEY, UUID.randomUUID().toString()), null, -1);
    this.authenticateService = authenticateService;
    //load  all  cache
    SessionCache.instance().add(Credential.CREDENTIAL_DEF_KEY, Credential.CREDENTIAL_ALL_KEY, authenticateService.loadAllCredentials());
  }

  /**
   * 从cookie读取用户信息
   *
   * @param request
   * @return
   */
  public Session in(HttpRequest request) {
    Session session = build(request);
    Session.setCurrent(session);
    return session;
  }

  /**
   * 保存session 信息
   *
   * @param request
   * @param session
   */
  public void buildSessionMetadata(HttpRequest request, Session session) {
    Session newSession = Session.current();
    if (isChangeSessionMetadata(session)) {
      updateSessionMetadata(request, session, newSession);
    } else {
      saveSessionMetadata(request, session);
    }

  }

  public boolean isChangeSessionMetadata(Session session) {
    Session newSession = Session.current();
    if (newSession != session) {
      String sessionKey = session.get(Session.SESSION_DEF_KEY);
      String newSessionKey = newSession.get(Session.SESSION_DEF_KEY);
      if (!sessionKey.equals(newSessionKey)) {
        return true;
      }
    }
    return false;
  }

  private void updateSessionMetadata(HttpRequest request, Session session, Session newSession) {
    String sessionKey = session.get(Session.SESSION_DEF_KEY);
    Principal principal = session.getPrincipal();
    if (principal != null) {
      sessions.remove(principal.getUsername(), sessionKey);
    } else {
      sessions.remove("anonymous@" + request.getClientAddress(), sessionKey);
    }
    saveSessionMetadata(request, newSession);
  }

  private void saveSessionMetadata(HttpRequest request, Session session) {
    Map<String, String> metadata = prepareSessionMetadata(request);
    String sessionKey = session.get(Session.SESSION_DEF_KEY);
    Principal principal = session.getPrincipal();
    if (principal != null) {
      String name = principal.getUsername();
      sessions.touch(name, sessionKey, metadata, session.getExpires());
    } else {
      sessions.touch("anonymous@" + request.getClientAddress(), sessionKey, metadata, session.getExpires());
    }
  }

  /**
   * 写出到cookie
   *
   * @param session
   * @param response
   */
  public Session out(Session session, HttpResponse response) {
    Session newSession = Session.current();
    if (newSession != session) {
      updateSessionInClient(response, newSession);
    }
    return newSession;
  }


  private Map<String, String> prepareSessionMetadata(HttpRequest req) {
    String agent = req.getHeader("User-Agent");
    return Maper.of(
        Sessions.ADDRESS_KEY, req.getClientAddress(),
        Sessions.AGENT_KEY, agent == null ? "Unknown" : agent);
  }

  private Session build(HttpRequest req) {
    String sessionCookieName = sessionCookieDescriptor.getCookieName();
    String cookie = req.getCookieValue(sessionCookieName);
    if (cookie == null || cookie.trim().isEmpty()) {
      return emptySession;
    } else {
      String sig = req.getCookieValue(sessionCookieDescriptor.getCookieSignatureName());
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
          if (sessionDatas == null || (sessionDatas != null && !sessionDatas.containsSessionKey(cookieValues.get(Session.SESSION_DEF_KEY)))) {
            return emptySession;
          }
          //是否使用cache
          if (cacheEnabled) {
            //通过cache 来获取对象相关的值
            principal = SessionCache.instance().get(Principal.PRINCIPAL_DEF_KEY, principalName);
            //cache 已经失效  从接口获取用户数据
            if (principal == null) {
              principal = authenticateService.findByUsername(principalName);
              if (principal != null)
                SessionCache.instance().add(Principal.PRINCIPAL_DEF_KEY, principalName, principal);
            }
          } else {
            principal = authenticateService.findByUsername(principalName);
          }

          //检测用户数据
          checkNotNull(principal, "FindByName not get user data.");
        }
        return new Session(cookieValues, principal, expiration);
      } else {
        return emptySession;
      }
    }
  }

  private Map<String, String> readEntries(String cookie) {
    return Jsoner.parseObject(cookie, Map.class);
  }

  private void updateSessionInClient(HttpResponse resp, Session session) {
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

  private Map<String, String> toCookiesMap(Session session) {
    Map<String, String> sessionMap = session.getValues();
    if (sessionMap.isEmpty()) {
      return Maper.of();
    } else {
      Map<String, String> map = Maper.copyOf(sessionMap);
      map.put(EXPIRES, Long.toString(session.getExpires()));
      String sessionJson = Jsoner.toJSONString(map);
      return Maper.of(sessionCookieDescriptor.getCookieName(), sessionJson,
          sessionCookieDescriptor.getCookieSignatureName(), signer.sign(sessionJson));
    }
  }

  public String toString() {
    return "SessionBuilder";
  }
}
