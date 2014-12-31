package cn.dreampie.example;

import cn.dreampie.orm.Model;
import cn.dreampie.util.json.Jsoner;
import org.junit.Test;

/**
 * Created by ice on 14-12-31.
 */
public class JsonTest {

  @Test
  public void testJson() {
    Demo demo = new Demo();
    demo.put("test", "test");
    String json = Jsoner.toJSONString(demo);
    Demo t = (Demo) Jsoner.parseObject(json, Model.class);
  }
}
