package cn.dreampie.security;

import cn.dreampie.common.Constant;
import cn.dreampie.log.Logger;
import cn.dreampie.cache.SimpleCache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static cn.dreampie.common.util.Checker.checkNotNull;

/**
 * Date: 17/11/13
 * Time: 16:23
 */
public class Sessions {

  public static final String ADDRESS_KEY = "_address";
  public static final String AGENT_KEY = "_agent";
  private static final Logger logger = Logger.getLogger(Sessions.class);

  /**
   * sessionKey->username
   */
  private final Map<String, String> usernames = new ConcurrentHashMap<String, String>();
  /**
   * username->(sessionKey,sessionData)
   */
  private final Map<String, SessionDatas> sessions = new ConcurrentHashMap<String, SessionDatas>();
  private final long defaultExpires;
  private final int limit;

  public Sessions(long defaultExpires, int limit) {
    this.defaultExpires = defaultExpires;
    this.limit = limit;
  }

  public SessionDatas get(String sessionKey) {
    SessionDatas sessionDatas = null;
    String username = null;
    if (Constant.cacheEnabled) {
      username = SimpleCache.instance().get(Session.SESSION_DEF_KEY, sessionKey);
      if (username != null) {
        sessionDatas = SimpleCache.instance().get(Session.SESSION_DEF_KEY, username);
      }
    } else {
      if (usernames.containsKey(sessionKey)) {
        username = usernames.get(sessionKey);
        sessionDatas = sessions.get(username);
      }
    }

    if (sessionDatas != null) {
      Map<String, SessionData> sessionMetadatas = sessionDatas.getSessionMetadatas();
      if (sessionMetadatas.size() > 0) {
        //删除超时的session
        removeTimeout(sessionMetadatas);

        //重新保存session数据
        saveSessionMetadatas(username, sessionKey, sessionDatas, sessionMetadatas);
      }
    }
    return sessionDatas;
  }

  /**
   * 保持session
   *
   * @param username
   * @param sessionDatas
   */
  private void save(String username, String sessionKey, SessionDatas sessionDatas) {
    //add cache
    if (Constant.cacheEnabled) {
      SimpleCache.instance().add(Session.SESSION_DEF_KEY, sessionKey, username);
      SimpleCache.instance().add(Session.SESSION_DEF_KEY, username, sessionDatas);
    } else {
      this.usernames.put(sessionKey, username);
      this.sessions.put(username, sessionDatas);
    }
    logger.info("Save session success. username was: %s. sessionKey was: %s.", username, sessionKey);
  }

  /**
   * 删除sessionKey和username的对应关系
   *
   * @param sessionKey
   */
  private void removeUsername(String sessionKey) {
    if (Constant.cacheEnabled) {
      SimpleCache.instance().remove(Session.SESSION_DEF_KEY, sessionKey);
    } else {
      usernames.remove(sessionKey);
    }
  }

  /**
   * 删除旧的session数据
   *
   * @param username
   * @param sessionKey
   */
  private void remove(String username, String sessionKey) {
    SessionDatas sessionDatas = get(sessionKey);
    if (sessionDatas != null) {
      Map<String, SessionData> sessionMetadatas = sessionDatas.getSessionMetadatas();
      if (sessionMetadatas.size() > 0) {
        sessionMetadatas.remove(sessionKey);
        //重新保存session数据
        saveSessionMetadatas(username, sessionKey, sessionDatas, sessionMetadatas);
      }
    }
  }

  /**
   * 保存session  剩余数据
   *
   * @param username
   * @param sessionKey
   * @param sessionDatas
   * @param sessionMetadatas
   */
  private void saveSessionMetadatas(String username, String sessionKey, SessionDatas sessionDatas, Map<String, SessionData> sessionMetadatas) {
    //一个session也没有
    if (sessionMetadatas.size() == 0) {
      removeUsername(sessionKey);
      if (Constant.cacheEnabled) {
        SimpleCache.instance().remove(Session.SESSION_DEF_KEY, username);
      } else {
        this.sessions.remove(username);
      }
    } else {
      if (Constant.cacheEnabled) {
        SimpleCache.instance().add(Session.SESSION_DEF_KEY, username, sessionDatas);
      } else {
        this.sessions.put(username, sessionDatas);
      }
    }
  }

  /**
   * 删除旧的session，生成新的session
   *
   * @param oldUsername
   * @param oldSessionKey
   * @param username
   * @param sessionKey
   * @param session
   * @return
   */
  public SessionDatas update(String oldUsername, String oldSessionKey, String username, String sessionKey, Session session) {
    //删除旧的session
    remove(oldUsername, oldSessionKey);
    return update(username, sessionKey, session);
  }

  public SessionDatas update(String username, String sessionKey, Session session) {
    //获取该用户名下的所有session
    SessionDatas sessionDatas = get(sessionKey);
    Map<String, SessionData> sessionMetadatas;
    SessionDatas updatedSessionDatas;
    SessionData sessionData;
    //save sessionData
    long access = System.currentTimeMillis();
    if (sessionDatas != null) {
      sessionMetadatas = sessionDatas.getSessionMetadatas();
      sessionData = sessionMetadatas != null && sessionMetadatas.size() > 0 ? sessionMetadatas.get(sessionKey) : null;

      if (sessionData != null) {
        //更新
        updatedSessionDatas = sessionDatas.touch(sessionKey, sessionData.touch(defaultExpires, session));
      } else {
        updatedSessionDatas = sessionDatas.touch(sessionKey, new SessionData(sessionKey, defaultExpires, access, access, System.nanoTime(), session));
      }
    } else {
      sessionMetadatas = new ConcurrentHashMap<String, SessionData>();
      sessionMetadatas.put(sessionKey, new SessionData(sessionKey, defaultExpires, access, access, System.nanoTime(), session));
      updatedSessionDatas = new SessionDatas(username, sessionMetadatas);
    }
    if (limit > 0) {
      //如果session已经到达限制数量
      boolean remove;
      do {
        remove = sessionMetadatas != null && sessionMetadatas.size() > limit;
        if (remove) {
          removeOldest(sessionMetadatas);
        }
      } while (remove);
    }
    //保存session
    save(username, sessionKey, updatedSessionDatas);
    return updatedSessionDatas;
  }

  /**
   * 删除超时的session
   *
   * @param sessionMetadatas
   */
  private void removeTimeout(Map<String, SessionData> sessionMetadatas) {
    //删除超时的session
    if (sessionMetadatas != null && sessionMetadatas.size() > 0) {
      boolean todoDel;
      do {
        todoDel = false;
        //判断是否存在超时的session
        for (String sk : sessionMetadatas.keySet()) {
          if (System.currentTimeMillis() > sessionMetadatas.get(sk).getExpires()) {
            todoDel = true;
            break;
          }
        }
        //delete oldest session
        if (todoDel) {
          removeOldest(sessionMetadatas);
        }
      } while (todoDel);
    }
  }

  /**
   * 删除时间最小的session
   *
   * @param sessionMetadatas
   */
  private void removeOldest(Map<String, SessionData> sessionMetadatas) {
    if (sessionMetadatas != null && sessionMetadatas.size() > 0) {
      List<SessionData> sessionDataList;
      SessionData oldest;
      sessionDataList = new ArrayList<SessionData>(sessionMetadatas.values());
      Collections.sort(sessionDataList);
      oldest = sessionDataList.get(0);
      // we remove it only if it hasn't changed. If it changed the remove method of ConcurrentMap won't
      // remove it, and we will go on with the while loop
      sessionMetadatas.remove(oldest.getSessionKey());
    }
  }

  public static final class SessionDatas implements Serializable {
    private final String username;
    private final Map<String, SessionData> sessionMetadatas;

    public SessionDatas(String username, Map<String, SessionData> sessionMetadatas) {
      this.username = checkNotNull(username);
      this.sessionMetadatas = checkNotNull(sessionMetadatas);
    }

    public String getUsername() {
      return username;
    }

    public SessionData getSessionData(String sessionKey) {
      return sessionMetadatas.get(sessionKey);
    }

    public boolean containsSessionKey(String sessionKey) {
      return sessionMetadatas.containsKey(sessionKey);
    }

    public Map<String, SessionData> getSessionMetadatas() {
      return sessionMetadatas;
    }

    private SessionDatas touch(String sessionKey, SessionData sessionData) {
      sessionMetadatas.put(sessionKey, sessionData);
      return new SessionDatas(username, sessionMetadatas);
    }

//    public boolean equals(Object o) {
//      if (this == o) return true;
//      if (o == null || getClass() != o.getClass()) return false;
//
//      SessionDatas that = (SessionDatas) o;
//      return username.equals(that.username) && sessionMetadatas.equals(that.getSessionMetadatas());
//    }

//    public int hashCode() {
//      return username.hashCode();
//    }

    public String toString() {
      return "SessionDatas{" +
          "username='" + username + '\'' +
          ", sessionMetadatas=" + sessionMetadatas +
          '}';
    }
  }

  public static final class SessionData implements Comparable<SessionData>, Serializable {
    private final String sessionKey;
    private final long firstAccess;
    private final long lastAccess;
    private final long lastAccessNano;
    private final long expires;
    private final Session session;

    public SessionData(String sessionKey, long defaultExpires, long firstAccess, long lastAccess, long lastAccessNano, Session session) {
      this.sessionKey = checkNotNull(sessionKey);
      this.firstAccess = firstAccess;
      this.lastAccess = lastAccess;
      this.lastAccessNano = lastAccessNano;
      this.session = checkNotNull(session);
      long sessionExpires = session.getExpires();
      if (sessionExpires == -1) {
        expires = System.currentTimeMillis() + defaultExpires;
      } else {
        expires = sessionExpires;
      }
    }

    public String getSessionKey() {
      return sessionKey;
    }

    public long getExpires() {
      return expires;
    }

    public long getFirstAccess() {
      return firstAccess;
    }

    public long getLastAccess() {
      return lastAccess;
    }

    public long getLastAccessNano() {
      return lastAccessNano;
    }

    public Session getSession() {
      return session;
    }

    private SessionData touch(long defaultExpires, Session session) {
      return new SessionData(sessionKey, defaultExpires, firstAccess, System.currentTimeMillis(), System.nanoTime(), session);
    }

    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      SessionData that = (SessionData) o;
      return sessionKey.equals(that.sessionKey);
    }
//
//    public int hashCode() {
//      return sessionKey.hashCode();
//    }

    public String toString() {
      return "SessionData{" +
          "sessionKey='" + sessionKey + '\'' +
          ", firstAccess=" + firstAccess +
          ", lastAccess=" + lastAccess +
          ", lastAccessNano=" + lastAccessNano +
          ", session=" + session +
          '}';
    }

    //升序
    public int compareTo(SessionData o) {
      return (lastAccessNano < o.lastAccessNano ? -1 : (lastAccessNano == o.lastAccessNano ? 0 : 1));
    }

  }
}
