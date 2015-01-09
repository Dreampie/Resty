package cn.dreampie.security;


import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.common.util.Maper;
import cn.dreampie.common.util.json.Jsoner;
import cn.dreampie.log.Logger;
import cn.dreampie.security.cache.SessionCache;
import cn.dreampie.security.sign.CookieSigner;
import cn.dreampie.security.sign.Signer;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

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
  private final AuthenticateService authenticateService;
  private final int expires;

  public SessionBuilder(int expires, int limit, int rememberDay, AuthenticateService authenticateService) {
    this(expires, limit, rememberDay, authenticateService, new DefaultPasswordService());
  }

  public SessionBuilder(int expires, int limit, int rememberDay, AuthenticateService authenticateService, PasswordService passwordService) {
    Subject.init(rememberDay, authenticateService, passwordService);
    this.expires = expires;
    this.sessions = new Sessions(limit);
    this.signer = new CookieSigner();
    this.sessionCookieDescriptor = new SessionCookieDescriptor();
    this.emptySession = new Session(Maper.<String, String>of(), null, -1);
    this.authenticateService = authenticateService;
    //load  all  cache
    SessionCache.instance().add(Permission.PERMISSION_DEF_KEY, Permission.PERMISSION_ALL_KEY, authenticateService.loadAllPermissions());
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

    Map<String, String> metadata = prepareSessionStatsMetadata(request);
    if (session.getPrincipal() != null) {
      String name = session.getPrincipal().getUsername();
      sessions.touch(name, metadata);
    } else {
      sessions.touch("anonymous@" + request.getClientAddress(), metadata);
    }
    return session;
  }

  /**
   * 写出到cookie
   *
   * @param session
   * @param response
   */
  public void out(Session session, HttpResponse response) {

    Session newSession = Session.current();
    if (newSession.getExpires() == -1 || newSession != session) {
      updateSessionInClient(response, newSession);
    }
  }


  private Map<String, String> prepareSessionStatsMetadata(HttpRequest req) {
    String agent = req.getHeader("User-Agent");
    return Maper.of(
        "clientAddress", req.getClientAddress(),
        "userAgent", agent == null ? "Unknown" : agent);
  }

  private Session build(HttpRequest req) {
    String sessionCookieName = sessionCookieDescriptor.getCookieName();
    String cookie = req.getCookieValue(sessionCookieName);
    if (cookie == null || cookie.trim().isEmpty()) {
      return emptySession;
    } else {
      String sig = req.getCookieValue(sessionCookieDescriptor.getCookieSignatureName());
      if (sig == null || !signer.verify(cookie, sig)) {
        logger.warn("Invalid  session signature. session was: %s. Ignoring session cookie.", cookie);
        return emptySession;
      }
      Map<String, String> entries = readEntries(cookie);
      String expiresCookie = entries.remove(EXPIRES);
      //失效时间
      if (expiresCookie != null && !"".equals(expiresCookie.trim())) {
        Date expires = new Date(Long.parseLong(expiresCookie));
        if (expires.getTime() < System.currentTimeMillis()) {
          return emptySession;
        }

        Date now = new Date();
        int expiration = (int) (expires.getTime() - now.getTime());
        expiration = req.isPersistentCookie(sessionCookieName) ? (expiration > this.expires ? expiration : -1) : -1;
        Map<String, String> cookieValues = Maper.copyOf(entries);
        String principalName = cookieValues.get(Principal.PRINCIPAL_DEF_KEY);
        Principal principal = null;
        if (principalName != null && !"".equals(principalName.trim())) {
          //通过cache 来获取对象相关的值
          principal = SessionCache.instance().get(Principal.PRINCIPAL_DEF_KEY, principalName);
          //cache 已经失效  从接口获取用户数据
          if (principal == null) {
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
        resp.addCookie(cookie.getKey(), cookie.getValue(), session.getExpires());
      }
    }
  }

  private Map<String, String> toCookiesMap(Session session) {
    Map<String, String> sessionMap = session.getValues();
    if (sessionMap.isEmpty()) {
      return Maper.of();
    } else {
      Map<String, String> map = Maper.copyOf(sessionMap);
      long expiresReal = session.getExpires();
      if (session.getExpires() == -1) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MILLISECOND, expires);
        expiresReal = cal.getTimeInMillis();
      }
      map.put(EXPIRES, Long.toString(expiresReal));
      String sessionJson = Jsoner.toJSONString(map);
      return Maper.of(sessionCookieDescriptor.getCookieName(), sessionJson,
          sessionCookieDescriptor.getCookieSignatureName(), signer.sign(sessionJson));
    }
  }

  public String toString() {
    return "SessionBuilder";
  }
}
