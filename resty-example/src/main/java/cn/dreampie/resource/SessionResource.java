package cn.dreampie.resource;

import cn.dreampie.route.core.annotation.API;
import cn.dreampie.route.core.annotation.DELETE;
import cn.dreampie.route.core.annotation.POST;
import cn.dreampie.security.Principal;
import cn.dreampie.security.Subject;
import cn.dreampie.resource.user.model.*;

/**
 * Created by wangrenhui on 15/1/10.
 */
@API("/sessions")
public class SessionResource extends ApiResource {


  @POST(des = "用户登录", valid = SigninValid.class)
  public User login(String username, String password, boolean rememberMe) {
    Subject.login(username, password);
    return (User)Subject.getPrincipal().getModel();
  }

  @DELETE(des = "用户退出")
  public void logout() {
    Subject.logout();
  }
}
