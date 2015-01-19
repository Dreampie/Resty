package cn.dreampie.config;

import cn.dreampie.security.AuthenticateService;
import cn.dreampie.security.Credential;
import cn.dreampie.security.DefaultPasswordService;
import cn.dreampie.security.Principal;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by ice on 15-1-7.
 */
public class MyAuthenticateService implements AuthenticateService {

  public Principal findByUsername(String username) {
    DefaultPasswordService defaultPasswordService = new DefaultPasswordService();

    Principal principal = new Principal(username, defaultPasswordService.hash("123"), new HashSet<String>() {{
      add("users");
    }});
    return principal;
  }

  public Set<Credential> loadAllCredentials() {
    Set<Credential> credentials = new HashSet<Credential>();
    credentials.add(new Credential("GET", "/api/v1.0/users**", "users"));
    return credentials;
  }
}
