package cn.dreampie.security;

import cn.dreampie.common.util.Maper;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by wangrenhui on 14/12/23.
 */
public class Session implements Serializable {
  public static final String SESSION_DEF_KEY = "_session";
  private final Map<String, String> values;
  private final String sessionKey;
  private final String username;
  private final long expires;

  public Session(String sessionKey, String username) {
    this(sessionKey, username, Maper.<String, String>of());
  }

  public Session(String sessionKey, String username, Map<String, String> values) {
    this(sessionKey, username, values, -1);
  }

  public Session(String sessionKey, String username, Map<String, String> values, long expires) {
    this.sessionKey = sessionKey;
    this.username = username;
    this.values = values;
    this.expires = expires;
  }

  String getSessionKey() {
    return sessionKey;
  }

  long getExpires() {
    return expires;
  }

  String getUsername() {
    return username;
  }

  Map<String, String> getValues() {
    return values;
  }

  String get(String key) {
    return values.get(key);
  }

  void set(Map<String, String> values) {
    Set<Map.Entry<String, String>> entrySet = values.entrySet();
    for (Map.Entry<String, String> entry : entrySet) {
      set(entry.getKey(), entry.getValue());
    }
  }

  void set(String key, String value) {
    if (value == null) {
      values.remove(key);
    } else {
      values.put(key, value);
    }
  }

  String remove(String key) {
    return values.remove(key);
  }
}
