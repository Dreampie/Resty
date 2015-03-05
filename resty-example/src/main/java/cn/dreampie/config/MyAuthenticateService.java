package cn.dreampie.config;

import cn.dreampie.resource.user.model.UserInfo;
import cn.dreampie.security.AuthenticateService;
import cn.dreampie.security.Credential;
import cn.dreampie.security.DefaultPasswordService;
import cn.dreampie.security.Principal;
import cn.dreampie.resource.user.model.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by ice on 15-1-7.
 */
public class MyAuthenticateService implements AuthenticateService {

  public Principal findByUsername(String username) {
    DefaultPasswordService defaultPasswordService = new DefaultPasswordService();

    User u = new User().set("username", username).set("password", defaultPasswordService.hash("123")).put("permissions", new HashSet<String>() {{
      add("users");
    }});

    Principal<User> principal = new Principal<User>(u.getStr("username"), u.getStr("password"), (Set) u.get("permissions"), u);
    return principal;
  }

  public Set<Credential> loadAllCredentials() {
    Set<Credential> credentials = new HashSet<Credential>();
    credentials.add(new Credential("*", "/api/v1.0/users**", "users"));
    return credentials;
  }
}
