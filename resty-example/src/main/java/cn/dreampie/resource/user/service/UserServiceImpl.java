package cn.dreampie.resource.user.service;

import cn.dreampie.resource.user.model.User;
import cn.dreampie.resource.user.model.UserInfo;

/**
 * Created by wangrenhui on 15/1/2.
 */
public class UserServiceImpl implements UserService {

  public User save(User u) {
    UserInfo userInfo = null;
    if (u.get("user_info") == null) {
      userInfo = new UserInfo().set("gender", 0);
    } else {
      userInfo = u.get("user_info");
    }
    if (u.set("providername", "x").set("password", "123").set("sid", "2").save()) {
      userInfo.set("user_id", u.get("id"));
      userInfo.save();
    }
    update(u);
//    if (u != null) {
//      throw new RuntimeException("xx");
//    }
    return u;
  }

  public User update(User u) {
    u.update();
    return u;
  }
}
