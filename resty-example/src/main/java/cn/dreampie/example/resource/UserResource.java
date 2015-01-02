package cn.dreampie.example.resource;

import cn.dreampie.example.model.User;
import cn.dreampie.example.service.UserService;
import cn.dreampie.example.service.UserServiceImpl;
import cn.dreampie.orm.transaction.AspectFactory;
import cn.dreampie.orm.transaction.TransactionAspect;
import cn.dreampie.route.core.annotation.API;
import cn.dreampie.route.core.annotation.GET;
import cn.dreampie.route.core.annotation.POST;
import cn.dreampie.route.core.base.Resource;
import cn.dreampie.util.Maper;

import java.util.Map;

/**
 * Created by ice on 14-12-29.
 */
@API("/api")
public class UserResource extends Resource {

  private UserService userService = AspectFactory.newInstance(new UserServiceImpl(), new TransactionAspect());

  @GET("/users/:name")
  public Map find(String name) {
    return Maper.of("k1", "v1,name:" + name, "k2", "v2");
  }

  @POST("/users")
  public User save(User user) {
    userService.save(user);
    return user;
  }

  @GET("/users")
  public User transaction() {
    return userService.save(new User().set("username", "test").set("providername", "test").set("password", "123456"));
  }
}
