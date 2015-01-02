package cn.dreampie.example.service;

import cn.dreampie.example.model.User;
import cn.dreampie.example.model.UserInfo;

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
    if (u.save()) {
      userInfo.set("user_id", u.get("id"));
      userInfo.save();
      int[] a = new int[0];
      System.out.println(a[2]);
    }
    return u;
  }
}
