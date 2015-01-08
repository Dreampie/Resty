package cn.dreampie.security;

import cn.dreampie.common.util.Maper;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static cn.dreampie.common.util.Checker.checkNotNull;

/**
 * Date: 17/11/13
 * Time: 16:23
 */
public class Sessions {
  public static final class SessionData implements Comparable<SessionData> {

    private final String key;
    private final long firstAccess;
    private final long lastAccess;
    private final long lastAccessNano;
    private final int count;
    private final Map<String, String> metadata;

    private SessionData(String key, long firstAccess, long lastAccess, long lastAccessNano, int count, Map<String, String> metadata) {
      this.key = checkNotNull(key);
      this.firstAccess = firstAccess;
      this.lastAccess = lastAccess;
      this.lastAccessNano = lastAccessNano;
      this.count = count;
      this.metadata = checkNotNull(metadata);
    }

    public String getKey() {
      return key;
    }

    public long getFirstAccess() {
      return firstAccess;
    }

    public long getLastAccess() {
      return lastAccess;
    }

    public int getCount() {
      return count;
    }

    public Map<String, String> getMetadata() {
      return metadata;
    }

    private SessionData touch(Map<String, String> metadata) {
      return new SessionData(key, firstAccess, System.currentTimeMillis(), System.nanoTime(), count + 1, metadata);
    }

    public String toString() {
      return "SessionData{" +
          "key='" + key + '\'' +
          ", firstAccess=" + firstAccess +
          ", lastAccess=" + lastAccess +
          ", metadata=" + metadata +
          '}';
    }

    public int compareTo(SessionData o) {
      return (int) (lastAccessNano - o.lastAccessNano);
    }

    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      SessionData that = (SessionData) o;

      if (!key.equals(that.key)) return false;

      return true;
    }

    public int hashCode() {
      return key.hashCode();
    }

  }

  private final ConcurrentMap<String, SessionData> sessions = new ConcurrentHashMap<String, SessionData>();
  private final int limit;

  public Sessions(int limit) {
    this.limit = limit;
  }

  public SessionData get(String key) {
    return sessions.get(key);
  }

  public Map<String, SessionData> getAll() {
    return Maper.copyOf(sessions);
  }

  public SessionData touch(String key, Map<String, String> metadata) {
    boolean updated = false;
    SessionData updatedSessionData;
    do {
      SessionData sessionData;
      sessionData = sessions.size() > 0 ? sessions.get(key) : null;
      if (sessionData != null) {
        updatedSessionData = sessionData.touch(metadata);
      } else {
        long access = System.currentTimeMillis();
        updatedSessionData = new SessionData(key, access, access, System.nanoTime(), 1, metadata);
      }

      updated = sessions.put(key, updatedSessionData) == sessionData;
    } while (!updated);

    // take size under limit
    // note that it may exceed the limit for a short time until the following code completes
    int size = sessions.size();
    int remainingChecks = (size - limit) * 3 + 100;
    while (sessions.size() > limit) {
      if (remainingChecks-- == 0) {
        // we have tried too many times to remove exceeding elements.
        // the possible cause is that oldest element is always updated between we find it and try to remove it
        // this is very unlikely but it's better to fail than run into an infinite loop

        throw new IllegalStateException(
            String.format(
                "Didn't manage to limit the size of sessions data within a reasonnable (%d) number of attempts",
                (size - limit) * 3 + 100));
      }

      Collection<SessionData> sessionDatas = sessions.values();
      SessionData oldest = sessionDatas.toArray(new SessionData[sessionDatas.size()])[0];

      // we check if we still need to remove an element, the sessions may have changed while we were
      // looking for the oldest element
      if (sessions.size() > limit) {
        // we remove it only if it hasn't changed. If it changed the remove method of ConcurrentMap won't
        // remove it, and we will go on with the while loop
        sessions.remove(oldest.getKey(), oldest);
      }
    }

    return updatedSessionData;
  }

  public static void main(String[] args) {
    ConcurrentMap<String, String> sessions = new ConcurrentHashMap<String, String>();
    sessions.put("a", "1");
    sessions.put("b", "2");
    Collection<String> sessionDatas = sessions.values();
    String oldest = sessionDatas.toArray(new String[sessionDatas.size()])[0];

    System.out.println(oldest);
  }
}
