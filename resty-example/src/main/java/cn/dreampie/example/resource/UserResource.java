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

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by ice on 14-12-29.
 */
@API("/api")
public class UserResource extends Resource {
  //用于使用service层的 事务
  // @Transaction(name = {"default", "demo"})的注解需要写在service的接口上
  // 注意java的自动代理必须存在接口
  private UserService userService = AspectFactory.newInstance(new UserServiceImpl(), new TransactionAspect());

  @GET("/users/:name/:age")
  public Map find(String name, int age) {
//    return Lister.of(name);
    return Maper.of("k1", "v1,name:" + name + ",age:" + age, "k2", "v2");
  }

  @GET("/users")
  public List<User> findAll() {
    return User.dao.findAll();
  }

  @POST("/users")
  public User save(User user) {
    userService.save(user);
    return user;
  }

  @GET("/transactions")
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
//      int[] a = new int[0];
//      System.out.println(a[2]);  报错 让事务回滚
    }
    return u;
    //service层的事务
    //return userService.save(new User().set("username", "test").set("providername", "test").set("password", "123456"));
  }

  @GET("/files/{filename}")
  public File file(String filename) {
    return new File(this.getClass().getClassLoader().getResource("application.properties").getFile());
  }
}
