package cn.dreampie.example;

import cn.dreampie.common.util.json.Jsoner;
import cn.dreampie.resource.user.model.User;
import com.alibaba.fastjson.JSON;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

/**
 * Created by ice on 14-12-31.
 */
public class JsonTest {
  public static void main(String[] args) {
    System.out.println(JSON.parseObject(JSON.toJSONString("2014-03-10 00:00:00"), Date.class));
  }

  @Before
  public void setUp() throws Exception {
    ActiveRecord.init();
  }

  @Test
  public void testJson() {
//    Record recordDAO =new Record("sec_user",true);
//    Record user = recordDAO.reNew().set("id", 1);
    User user = new User().set("id", 1);


//    String json = Jsoner.toJSON(user);
//    System.out.println(json);
//    User t = Jsoner.toObject("{\"key\":\"value\"}", User.class);
//    System.out.println(t.get("key"));
//
//    String[] a = Jsoner.toObject(Jsoner.toJSON(new String[]{"a", "b", "c"}), String[].class);

//    String json = Jsoner.toJSON(user);
//    System.out.println(json);
//    final User u = Jsoner.toObject(json, User.class);

//    System.out.println("" + u.getRoleId() + u.getUserInfos() + u.getPermissions() + u.getPermissionIds());

//    Map<String, Date> map = new HashMap<String, Date>();
//    map.put("date", new Timestamp(new Date().getTime()));
//    System.out.println(Jsoner.toJSON(map));
//    Map<String, Long> m = Jsoner.toObject("{k:1}", new TypeReference<Map<String, Long>>() {
//    });

//    System.out.println(Jsoner.toJSON(new File("/")));
//    try {
//      File file = new File("/Users/Dreampie/Pictures/thumb_IMG_1589_1024.jpg");
//      System.out.println(Jsoner.toJSON(ImageIO.get(file)));
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//


    System.out.println(Jsoner.toObject("\"2013-09-10\"", Date.class));
  }
}
