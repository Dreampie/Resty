package cn.dreampie.example;

import cn.dreampie.demo.model.Role;
import cn.dreampie.example.model.User;
import cn.dreampie.example.model.UserInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * Created by ice on 15-1-4.
 */
@FixMethodOrder(MethodSorters.JVM)
public class SqlTest {
  @Before
  public void setUp() throws Exception {
    ActiveRecord.init();
  }

  @Test
  public void testSave() {
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
    }
  }

  @Test
  public void testFind() {
    User u = User.dao.findById(1);
  }

  @Test
  public void testUpdate() {
    User u = User.dao.findById(1);
    u.set("username", "testupdate");
  }

  @Test
  public void testDelete() {
    User u = User.dao.findById(1);
    u.delete();
  }
}
