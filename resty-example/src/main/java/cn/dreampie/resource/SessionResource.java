package cn.dreampie.resource;

import cn.dreampie.route.core.annotation.DELETE;
import cn.dreampie.route.core.annotation.POST;
import cn.dreampie.security.Principal;
import cn.dreampie.security.Subject;

/**
 * Created by wangrenhui on 15/1/10.
 */
public class SessionResource extends ApiResource {


  @POST("/sessions")
  public Principal login(String username, String password, boolean rememberMe) {
    Subject.login(username, password);
    return Subject.getPrincipal();
  }

  @DELETE("/sessions")
  public void logout() {
    Subject.logout();
  }
}
