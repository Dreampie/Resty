package cn.dreampie.security;

import cn.dreampie.common.Constant;
import cn.dreampie.common.util.Maper;
import cn.dreampie.security.cache.SessionCache;

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

  public static final class SessionDatas {
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

    public String toString() {
      return "SessionDatas{" +
          "key='" + key + '\'' +
          ", sessionMetadatas=" + sessionMetadatas +
          '}';
    }
  }

  public static final class SessionData implements Comparable<SessionData> {
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

    public int hashCode() {
      return sessionKey.hashCode();
    }

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
      return (int) (lastAccessNano - o.lastAccessNano);
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

  public SessionDatas get(String key) {
    return getSessions().get(key);
  }

  public void remove(String key, String sessionKey) {
    Map<String, SessionDatas> sessions = getSessions();
    if (sessions.size() > 0) {
      SessionDatas sessionDatas = sessions.get(key);
      if (sessionDatas != null) {
        Map<String, SessionData> sessionMetadatas = sessionDatas.getSessionMetadatas();
        sessionMetadatas.remove(sessionKey);
        if (sessionMetadatas.size() <= 0) {
          sessions.remove(key);
        }
      }
    }
  }

  public Map<String, SessionDatas> getSessions() {
    Map<String, SessionDatas> sessionsUse = null;
    if (Constant.cache_enabled) {
      Map<String, SessionDatas> sessionsCache = SessionCache.instance().get(Session.SESSION_DEF_KEY, Session.SESSION_ALL_KEY);
      if (sessionsCache == null)
        sessionsUse = sessions;
    } else {
      sessionsUse = sessions;
    }
    return sessionsUse;
  }

  public Map<String, SessionDatas> getAll() {
    return Maper.copyOf(getSessions());
  }

  public SessionDatas touch(String key, String sessionKey, Map<String, String> metadata) {
    return touch(key, sessionKey, metadata, System.currentTimeMillis() + expires);
  }

  public SessionDatas touch(String key, String sessionKey, Map<String, String> metadata, long expires) {
    if (expires == -1) return touch(key, sessionKey, metadata);
    Map<String, SessionDatas> sessions = getSessions();
    Map<String, SessionData> sessionMetadatas;
    SessionDatas sessionDatas;
    SessionDatas updatedSessionDatas;
    SessionData sessionData;
    //save sessionData
    sessionDatas = sessions.get(key);
    long access = System.currentTimeMillis();
    if (sessionDatas != null) {
      sessionMetadatas = sessionDatas.getSessionMetadatas();
      sessionData = sessionMetadatas.get(sessionKey);
      if (sessionMetadatas.size() > 0) {
        //删除超时的session
        removeTimeout(sessionMetadatas);
      }
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
    while (sessionMetadatas.size() > limit) {
      removeOldest(sessionMetadatas);
    }
    sessions.put(key, updatedSessionDatas);
    // take size under limit
    // note that it may exceed the limit for a short time until the following code completes
//    SessionDatas datas = null;
//    Map<String, SessionData> sessionDataMap = null;
//    //user key
//    int size = 0;
//    for (String k : sessions.keySet()) {
//      datas = sessions.get(k);
//      if (datas != null) {
//        sessionDataMap = datas.getSessionMetadatas();
//        if (sessionDataMap != null && sessionDataMap.size() > 0) {
//          //all session size
//          size += sessionDataMap.size();
//        }
//      }
//    }
//
//    int remainingChecks = (size - limit) * 3 + 100;
//    if (remainingChecks == 0) {
//      // we have tried too many times to remove exceeding elements.
//      // the possible cause is that oldest element is always updated between we find it and try to remove it
//      // this is very unlikely but it's better to fail than run into an infinite loop
//
//      throw new IllegalStateException(
//          String.format(
//              "Didn't manage to limit the size of sessions data within a reasonnable (%d) number of attempts",
//              (size - limit) * 3 + 100));
//    }
    //add cache
    SessionCache.instance().add(Session.SESSION_DEF_KEY, Session.SESSION_ALL_KEY, sessions);
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
      for (SessionData s : sessionDataList) {
        System.out.println(s.lastAccessNano);
      }
      System.out.println("------------");
      Collections.sort(sessionDataList);
      for (SessionData s : sessionDataList) {
        System.out.println(s.lastAccessNano);
      }
      oldest = sessionDataList.get(0);
      // we remove it only if it hasn't changed. If it changed the remove method of ConcurrentMap won't
      // remove it, and we will go on with the while loop
      sessionMetadatas.remove(oldest.getSessionKey());
    }
  }
}
