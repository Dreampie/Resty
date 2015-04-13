package cn.dreampie.config;

import cn.dreampie.resource.user.model.User;
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

  /**
   * 查询用户信息  这儿new一个用户对象来模拟
   * @param username 登录的用户名
   * @return 用户权限对象
   */
  public Principal findByUsername(String username) {
    DefaultPasswordService defaultPasswordService = new DefaultPasswordService();

    User u = new User().set("username", username).set("password", defaultPasswordService.hash("123")).put("permissions", new HashSet<String>() {{
      add("users");
    }});

    Principal<User> principal = new Principal<User>(u.getStr("username"), u.getStr("password"), (Set) u.get("permissions"), u);
    return principal;
  }

  /**
   * 加载全部的权限信息
   * @return 权限集合
   */
  public Set<Credential> loadAllCredentials() {
    Set<Credential> credentials = new HashSet<Credential>();
    credentials.add(new Credential("*", "/api/v1.0/users/**", "users"));
    return credentials;
  }
}
