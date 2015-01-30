package cn.dreampie.security;

import org.junit.Test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SessionsTest {

  @Test
  public void testSessionData() {
    String sessionKey = UUID.randomUUID().toString();
    long access = System.currentTimeMillis();
    Map<String, Sessions.SessionData> ss = new ConcurrentHashMap<String, Sessions.SessionData>();
    Sessions.SessionData a = new Sessions.SessionData(sessionKey, access + 10000, access, access, System.nanoTime(), new HashMap<String, String>());
    try {
      Thread.sleep(10000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    sessionKey = UUID.randomUUID().toString();
    access = System.currentTimeMillis();
    Sessions.SessionData b = new Sessions.SessionData(sessionKey, access + 10000, access, access, System.nanoTime(), new HashMap<String, String>());
    ss.put("a", a);
    ss.put("b", b);


    List<Sessions.SessionData> sl = new ArrayList<Sessions.SessionData>((ss.values()));

    for (Sessions.SessionData s : sl) {
      System.out.println(s.getLastAccessNano());
    }
    System.out.println("-----------------");
    Collections.sort(sl);
    for (Sessions.SessionData s : sl) {
      System.out.println(s.getLastAccessNano());
    }
    System.out.println("-----------------");

    Collections.sort(sl);
    for (Sessions.SessionData s : sl) {
      System.out.println(s.getLastAccessNano());
    }
    System.out.println("-----------------");
  }
}