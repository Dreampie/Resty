package cn.dreampie.example;

import cn.dreampie.route.core.annotation.GET;
import cn.dreampie.route.core.annotation.POST;
import cn.dreampie.route.core.base.Resource;
import cn.dreampie.util.Maper;

import java.util.Map;

/**
 * Created by ice on 14-12-29.
 */
public class UserResource extends Resource {

  @GET("/users/:name")
  public Map find(String name) {
    return Maper.of("k1", "v1,name:" + name, "k2", "v2");
  }

  @POST("/users")
  public User save(User user) {
    user.save();
    return user;
  }

  @GET("/users")
  public void noreturn() {
    System.out.println("no return.");
  }
}
