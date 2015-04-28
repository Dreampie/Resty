package cn.dreampie.security;

import cn.dreampie.common.util.Maper;

import java.util.Map;

/**
 * Created by wangrenhui on 14/12/23.
 */
public class Session {
  public static final String SESSION_DEF_KEY = "_session";
  public static final String SESSION_ALL_KEY = "_allSessions";
  private static final ThreadLocal<Session> current = new ThreadLocal<Session>();
  private final Map<String, String> values;
  private final Principal principal;
  private final long expires;

  public Session(Map<String, String> values, Principal principal, long expires) {
    this.values = values;
    this.principal = principal;
    this.expires = expires;
  }

  static void setCurrent(Session session) {
    if (session == null) {
      current.remove();
    } else {
      current.set(session);
    }
  }

  static Session current() {
    return current.get();
  }

  long getExpires() {
    return expires;
  }

  //------------------current session-------------------------------

  Session setExpires(long expires) {
    return updateCurrent(new Session(values, principal, expires));
  }

  Principal getPrincipal() {
    return principal;
  }

  Map<String, String> getValues() {
    return values;
  }

  private Session updateCurrent(Session newSession) {
    if (this == current()) {
      current.set(newSession);
    }
    return newSession;
  }

  String get(String key) {
    return values.get(key);
  }

  Session set(String key, String value) {
    // create new map by using a mutable map, not a builder, in case the the given entry overrides a previous one
    Map<String, String> newValues = Maper.copyOf(values);
    if (value == null) {
      newValues.remove(key);
    } else {
      newValues.put(key, value);
    }
    return updateCurrent(new Session(Maper.copyOf(newValues), principal, expires));
  }

  String remove(String key) {
    return values.remove(key);
  }

  Session authenticateAs(Principal principal) {
    return updateCurrent(new Session(values, principal, expires)).set(Principal.PRINCIPAL_DEF_KEY, principal.getUsername());
  }

  Session clearPrincipal() {
    return updateCurrent(new Session(values, null, expires)).set(Principal.PRINCIPAL_DEF_KEY, null);
  }
}
