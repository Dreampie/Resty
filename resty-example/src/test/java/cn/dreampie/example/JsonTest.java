package cn.dreampie.example;

import cn.dreampie.common.util.json.Jsoner;
import cn.dreampie.resource.user.model.User;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by ice on 14-12-31.
 */
public class JsonTest {
  @Before
  public void setUp() throws Exception {
    ActiveRecord.init();
  }

  @Test
  public void testJson() {
//    Record recordDAO = Record.use("sec_user");
//    Record user = recordDAO.reNew().set("id", 1);
    User user = new User().set("id", 1);


//    String json = Jsoner.toJSONString(user);
//    System.out.println(json);
//    User t = Jsoner.parseObject("{\"key\":\"value\"}", User.class);
//    System.out.println(t.get("key"));
//
//    String[] a = Jsoner.parseObject(Jsoner.toJSONString(new String[]{"a", "b", "c"}), String[].class);

    String json = Jsoner.toJSONString(user);
    System.out.println(json);
    final User u = Jsoner.parseObject(json, User.class);

//    System.out.println("" + u.getRoleId() + u.getUserInfos() + u.getPermissions() + u.getPermissionIds());

  }
}
