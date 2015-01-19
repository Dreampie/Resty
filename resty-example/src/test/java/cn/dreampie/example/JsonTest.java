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
    User user = new User();
    user.put("test", "test");
    String json = Jsoner.toJSONString(user);
    System.out.println(json);
    User t = Jsoner.parseObject("{\"key\":\"value\"}", User.class);
    System.out.println(t.get("key"));
  }
}
