package cn.dreampie.example.resource;

import cn.dreampie.demo.model.Role;
import cn.dreampie.example.model.User;
import cn.dreampie.example.model.UserInfo;
import cn.dreampie.example.service.UserService;
import cn.dreampie.example.service.UserServiceImpl;
import cn.dreampie.orm.transaction.AspectFactory;
import cn.dreampie.orm.transaction.Transaction;
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
  @Transaction(name = {"default", "demo"})
  public User transaction() {
    User u = new User().set("username", "test").set("providername", "test").set("password", "123456");
    UserInfo userInfo = null;
    if (u.get("user_info") == null) {
      userInfo = new UserInfo().set("gender", 0);
    } else {
      userInfo = u.get("user_info");
    }
    if (u.save()) {
      userInfo.set("user_id", u.get("id"));
      userInfo.save();

      Role role = new Role().set("name", "test").set("value", "xx");
      role.save();
      int[] a = new int[0];
      System.out.println(a[2]);
    }
    return u;
//    return userService.save(new User().set("username", "test").set("providername", "test").set("password", "123456"));
  }
}
