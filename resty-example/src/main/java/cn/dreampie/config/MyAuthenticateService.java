package cn.dreampie.config;

import cn.dreampie.security.AuthenticateService;
import cn.dreampie.security.DefaultPasswordService;
import cn.dreampie.security.Permission;
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

  public Set<Permission> loadAllPermissions() {
    Set<Permission> permissions = new HashSet<Permission>();
    permissions.add(new Permission("GET", "/api/v1.0/users**", "users"));
    return permissions;
  }
}
