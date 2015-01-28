package cn.dreampie.resource.user;

import cn.dreampie.orm.DS;
import cn.dreampie.orm.Page;
import cn.dreampie.orm.transaction.AspectFactory;
import cn.dreampie.orm.transaction.Transaction;
import cn.dreampie.orm.transaction.TransactionAspect;
import cn.dreampie.resource.ApiResource;
import cn.dreampie.resource.user.model.User;
import cn.dreampie.resource.user.model.UserInfo;
import cn.dreampie.resource.user.service.UserService;
import cn.dreampie.resource.user.service.UserServiceImpl;
import cn.dreampie.route.core.annotation.DELETE;
import cn.dreampie.route.core.annotation.GET;
import cn.dreampie.route.core.annotation.POST;
import cn.dreampie.route.core.annotation.PUT;

import java.util.List;
import java.util.Set;

/**
 * Created by ice on 14-12-29.
 */
public class UserResource extends ApiResource {
  //用于使用service层的 事务
  // @Transaction(name = {"default", "demo"})的注解需要写在service的接口上
  // 注意java的自动代理必须存在接口
  private UserService userService = AspectFactory.newInstance(new UserServiceImpl(), new TransactionAspect());

  //查询集合
  @GET("/users")
  public List<User> findAll() {
    return User.dao.findAll();
  }

  //查询单个user对象
  @GET("/users/:id")
  public User find(String id) {
    return User.dao.findById(id);
  }

  //全部对象分页 http://localhost:8081/api/v1.0/users/1/10
  @GET("/users/:pageNumber/:pageSize")
  public Page<User> paginate(int pageNumber, int pageSize) {
    return User.dao.paginateAll(pageNumber, pageSize);
  }

  //按条件分页 http://localhost:8081/api/v1.0/users/1/10/x
  @GET("/users/:pageNumber/:pageSize/:term")
  public Page<User> paginate(int pageNumber, int pageSize, String term) {
    return User.dao.paginateBy(pageNumber, pageSize, "username=?", term);
  }

  //更新
  @PUT("/users")
  public User put(User user) {
    user.update();
    return user;
  }

  //保存
  @POST("/users")
  public Set<User> save(Set<User> users) {
    userService.save(users.iterator().next());
    return users;
  }

  //删除
  @DELETE("/users/:id")
  public boolean put(String id) {
    return User.dao.deleteById(id);
  }


  @GET("/transactions")
  @Transaction(name = {DS.DEFAULT_DS_NAME})
  public User transaction() {
    User u = new User().set("username", "test").set("providername", "test").set("password", "123456").set("sid", "1");
    UserInfo userInfo = null;
    if (u.get("user_info") == null) {
      userInfo = new UserInfo().set("gender", 0);
    } else {
      userInfo = u.get("user_info");
    }
    if (u.save()) {
      userInfo.set("user_id", u.get("id"));
      userInfo.save();

//      int[] a = new int[0];
//      System.out.println(a[2]);  报错 让事务回滚
    }
    u.set("id", u.get("id")).set("username", "x").update();
    return u;
    //service层的事务
    //return userService.save(new User().set("username", "test").set("providername", "test").set("password", "123456"));
  }

}
