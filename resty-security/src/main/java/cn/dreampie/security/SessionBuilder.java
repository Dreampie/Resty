package cn.dreampie.security;


import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.common.util.Maper;
import cn.dreampie.log.Logger;
import cn.dreampie.security.credential.Credentials;

import java.util.UUID;

/**
 * Created by ice on 14-12-24.
 * session 构建和处理
 */
public abstract class SessionBuilder {
  public final static String ANONYMOUS = "anonymous";
  public final static String DEFAULT_SESSION_NAME = "SESSION";
  public static final int DEFAULT_EXPIRES = 30 * 60 * 1000;
  public static final int DEFAULT_REMEMBER_DAY = 7;
  public static final int DEFAULT_LIMIT = -1;
  private final static Logger logger = Logger.getLogger(SessionBuilder.class);
  protected final Sessions sessions;
  protected final String sessionName;

  public SessionBuilder(AuthenticateService authenticateService) {
    this(null, -1, -1, -1, authenticateService);
  }

  public SessionBuilder(String sessionName, AuthenticateService authenticateService) {
    this(sessionName, -1, -1, -1, authenticateService);
  }

  public SessionBuilder(int limit, AuthenticateService authenticateService) {
    this(null, -1, limit, -1, authenticateService);
  }

  public SessionBuilder(int limit, int rememberDay, AuthenticateService authenticateService) {
    this(null, -1, limit, rememberDay, authenticateService);
  }

  public SessionBuilder(long expires, int limit, int rememberDay, AuthenticateService authenticateService) {
    this(null, expires, limit, rememberDay, authenticateService);
  }

  public SessionBuilder(String sessionName, long expires, int limit, int rememberDay, AuthenticateService authenticateService) {
    if (expires < 0) {
      expires = DEFAULT_EXPIRES;
    }
    if (limit < 0) {
      limit = DEFAULT_LIMIT;
    }
    if (rememberDay < 0) {
      rememberDay = DEFAULT_REMEMBER_DAY;
    }

    if (sessionName != null && !sessionName.isEmpty()) {
      this.sessionName = sessionName;
    } else {
      this.sessionName = DEFAULT_SESSION_NAME;
    }
    this.sessions = new Sessions(expires, limit);
    Subject.init(rememberDay, new Credentials(authenticateService, expires), authenticateService.getPasswordService());
  }


  /**
   * 构建session数据
   *
   * @param request
   * @return
   */
  protected Session getAnonymousSession(HttpRequest request, Session session) {
    String agent = request.getHeader("User-Agent");
    session.set(Maper.of(Sessions.ADDRESS_KEY, request.getClientAddress(),
        Sessions.AGENT_KEY, agent == null ? "Unknown" : agent));
    return session;
  }

  /**
   * 取匿名用户的名字
   *
   * @param session
   * @return
   */
  protected String getAnonymousName(Session session) {
    return ANONYMOUS + "@" + session.get(Sessions.ADDRESS_KEY);
  }


  /**
   * 读取session
   *
   * @param request
   * @return
   */
  public Session in(HttpRequest request, HttpResponse response) {
    Session session = null;
    String sessionKey = inputSessionKey(request);
    if (sessionKey != null) {
      logger.debug("Session key was: %s.", sessionKey);

      //判断 是否使用了 remeberme 或失效
      Sessions.SessionDatas sessionDatas = sessions.get(sessionKey);
      if (sessionDatas != null) {
        Sessions.SessionData sessionData = sessionDatas.getSessionData(sessionKey);
        if (sessionData != null) {
          session = sessionData.getSession();
          logger.debug("Found session success. username was: %s. session key was: %s.", session.getPrincipal().getUsername(), sessionKey);
        }
      } else {
        logger.warn("Invalid user session. session key was: %s. ignoring session.", sessionKey);
      }
    } else {
      sessionKey = UUID.randomUUID().toString();
      outputSessionKey(response, sessionKey, -1);//first init session
    }

    if (session == null) {
      session = getAnonymousSession(request, new Session(sessionKey));
    }

    Subject.updateCurrent(session);
    return session;
  }

  /**
   * 更新session
   *
   * @param oldSession
   * @param response
   */
  public Session out(Session oldSession, HttpResponse response) {
    Session session = Subject.current();
    String username;
    String sessionKey = session.getSessionKey();
    Principal principal = session.getPrincipal();

    if (principal != null) {
      username = principal.getUsername();
    } else {
      username = getAnonymousName(session);
    }
    if (session != oldSession) {
      String oldSessionKey = oldSession.getSessionKey();
      //重新存储session数据
      String oldUsername = null;
      Principal oldPrincipal = oldSession.getPrincipal();
      //原本的session
      if (oldPrincipal != null) {
        oldUsername = oldPrincipal.getUsername();
      } else {
        oldUsername = getAnonymousName(oldSession);
      }

      //登录或切换了用户
      if (!oldUsername.equals(username)) {
        //可能有 -1,0
        int expires = session.getExpires() > 0 ? (int) (session.getExpires() / 1000) : (int) session.getExpires();
        outputSessionKey(response, sessionKey, expires);
      }

      //现在的session
      sessions.update(oldUsername, oldSessionKey, username, sessionKey, session);
    } else {
      sessions.update(username, sessionKey, session);
    }
    return session;
  }

  public abstract String inputSessionKey(HttpRequest request);

  protected abstract void outputSessionKey(HttpResponse response, String sessionKey, int expires);

}
