package cn.dreampie.resource;

import cn.dreampie.route.core.annotation.API;
import cn.dreampie.route.core.annotation.DELETE;
import cn.dreampie.route.core.annotation.POST;
import cn.dreampie.security.Principal;
import cn.dreampie.security.Subject;

/**
 * Created by wangrenhui on 15/1/10.
 */
@API("/sessions")
public class SessionResource extends ApiResource {


  @POST(des = "用户登录")
  public Principal login(String username, String password, boolean rememberMe) {
    Subject.login(username, password);
    return Subject.getPrincipal();
  }

  @DELETE(des = "用户退出")
  public void logout() {
    Subject.logout();
  }
}
