package cn.dreampie.security;

import cn.dreampie.common.Constant;
import cn.dreampie.security.cache.SessionCache;

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

  public static final class SessionDatas implements Serializable {
    private final String key;
    private final Map<String, SessionData> sessionMetadatas;

    public SessionDatas(String key, Map<String, SessionData> sessionMetadatas) {
      this.key = checkNotNull(key);
      this.sessionMetadatas = checkNotNull(sessionMetadatas);
    }

    public String getKey() {
      return key;
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
      return new SessionDatas(key, sessionMetadatas);
    }

//    public boolean equals(Object o) {
//      if (this == o) return true;
//      if (o == null || getClass() != o.getClass()) return false;
//
//      SessionDatas that = (SessionDatas) o;
//      return key.equals(that.key) && sessionMetadatas.equals(that.getSessionMetadatas());
//    }

//    public int hashCode() {
//      return key.hashCode();
//    }

    public String toString() {
      return "SessionDatas{" +
          "key='" + key + '\'' +
          ", sessionMetadatas=" + sessionMetadatas +
          '}';
    }
  }

  public static final class SessionData implements Comparable<SessionData>, Serializable {
    private final String sessionKey;
    private final long expires;
    private final long firstAccess;
    private final long lastAccess;
    private final long lastAccessNano;
    private final Map<String, String> metadata;

    public SessionData(String sessionKey, long expires, long firstAccess, long lastAccess, long lastAccessNano, Map<String, String> metadata) {
      this.sessionKey = checkNotNull(sessionKey);
      this.expires = expires;
      this.firstAccess = firstAccess;
      this.lastAccess = lastAccess;
      this.lastAccessNano = lastAccessNano;
      this.metadata = checkNotNull(metadata);
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

    public Map<String, String> getMetadata() {
      return metadata;
    }

    private SessionData touch(long expires, Map<String, String> metadata) {
      return new SessionData(sessionKey, expires, firstAccess, System.currentTimeMillis(), System.nanoTime(), metadata);
    }

    private SessionData touch(Map<String, String> metadata) {
      return touch(expires, metadata);
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
          ", metadata=" + metadata +
          '}';
    }

    //升序
    public int compareTo(SessionData o) {
      return (lastAccessNano < o.lastAccessNano ? -1 : (lastAccessNano == o.lastAccessNano ? 0 : 1));
    }

  }

  /**
   * username->(sessionKey,sessionData)
   */
  private final Map<String, SessionDatas> sessions = new ConcurrentHashMap<String, SessionDatas>();
  private final int expires;
  private final int limit;

  public static final String ADDRESS_KEY = "_address";
  public static final String AGENT_KEY = "_agent";

  public Sessions(int expires, int limit) {
    this.expires = expires;
    this.limit = limit;
  }

  public void remove(String key, String sessionKey) {
    SessionDatas sessions = getSessions(key);
    if (sessions != null) {
      Map<String, SessionData> sessionMetadatas = sessions.getSessionMetadatas();
      if (sessionMetadatas.size() > 0) {
        sessionMetadatas.remove(sessionKey);
      }
      if (Constant.cacheEnabled) {
        SessionCache.instance().add(Session.SESSION_DEF_KEY, key, sessions);
      }
    }
  }

  public SessionDatas getSessions(String key) {
    SessionDatas sessionsUse = null;
    if (Constant.cacheEnabled) {
      sessionsUse = SessionCache.instance().get(Session.SESSION_DEF_KEY, key);
    } else {
      sessionsUse = sessions.get(key);
    }
    if (sessionsUse != null) {
      Map<String, SessionData> sessionMetadatas = sessionsUse.getSessionMetadatas();
      if (sessionMetadatas.size() > 0) {
        //删除超时的session
        removeTimeout(sessionMetadatas);
      }
    }
    return sessionsUse;
  }

  private boolean saveSessions(String key, SessionDatas sessionDatas) {
    boolean updated = false;
    //add cache
    if (Constant.cacheEnabled) {
      SessionCache.instance().add(Session.SESSION_DEF_KEY, key, sessionDatas);
      updated = SessionCache.instance().get(Session.SESSION_DEF_KEY, key) != null;
    } else {
      updated = this.sessions.put(key, sessionDatas) != null;
    }
    return updated;
  }

  public SessionDatas touch(String key, String sessionKey, Map<String, String> metadata) {
    return touch(key, sessionKey, metadata, System.currentTimeMillis() + expires);
  }

  public SessionDatas touch(String key, String sessionKey, Map<String, String> metadata, long expires) {
    if (expires == -1) return touch(key, sessionKey, metadata);
    //获取该用户名下的所有session
    SessionDatas sessionDatas = getSessions(key);
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
        updatedSessionDatas = sessionDatas.touch(sessionKey, sessionData.touch(expires, metadata));
      } else {
        updatedSessionDatas = sessionDatas.touch(sessionKey, new SessionData(sessionKey, expires, access, access, System.nanoTime(), metadata));
      }
    } else {
      sessionMetadatas = new ConcurrentHashMap<String, SessionData>();
      sessionMetadatas.put(sessionKey, new SessionData(sessionKey, expires, access, access, System.nanoTime(), metadata));
      updatedSessionDatas = new SessionDatas(key, sessionMetadatas);
    }
    //如果session已经到达限制数量
    while (sessionMetadatas != null && sessionMetadatas.size() > limit) {
      removeOldest(sessionMetadatas);
    }
    boolean updated = false;
    do {
      updated = saveSessions(key, updatedSessionDatas);
    } while (!updated);

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
        if (todoDel)
          removeOldest(sessionMetadatas);
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
}
