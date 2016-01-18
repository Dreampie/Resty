package cn.dreampie.resource;

import cn.dreampie.resource.user.model.User;
import cn.dreampie.route.annotation.API;
import cn.dreampie.route.annotation.DELETE;
import cn.dreampie.route.annotation.POST;
import cn.dreampie.security.Subject;

/**
 * Created by wangrenhui on 15/1/10.
 */
@API("/sessions")
public class SessionResource extends ApiResource {


  @POST(des = "用户登录", valid = SigninValidator.class)
  public User login(String username, String password, boolean rememberMe) {
    Subject.login(username, password);
    return (User) Subject.getPrincipal().getModel();
  }

  @DELETE(des = "用户退出")
  public void logout() {
    Subject.logout();
  }
}
