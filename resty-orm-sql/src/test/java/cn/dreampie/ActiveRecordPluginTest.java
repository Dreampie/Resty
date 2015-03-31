package cn.dreampie;


import cn.dreampie.orm.ActiveRecordPlugin;
import cn.dreampie.orm.Record;
import cn.dreampie.orm.provider.druid.DruidDataSourceProvider;
import org.junit.Test;

import java.util.Date;

/**
 * Unit test for activeRecord plugin.
 */
public class ActiveRecordPluginTest {
  @Test
  public void testStart() {
    ActiveRecordPlugin activeRecordPlugin = new ActiveRecordPlugin(new DruidDataSourceProvider("default"));
//    activeRecordPlugin.addIncludePaths("cn.dremapie.orm");
    activeRecordPlugin.start();

    Record recordDAO = Record.use("sec_user","id,sid");

    Record r = recordDAO.reNew().set("sid", 10).set("username", "x").set("password","123").set("providername","default").set("created_at", new Date());
    r.save();

    r = Record.use("sec_user").findColsByIds("id,sid", new Object[]{r.get("id"), r.get("sid")});

    r.update();
    //Record 解析支持
//    Jsoner.addConfig(Record.class, ModelSerializer.instance(), ModelDeserializer.instance());
//    Record r = new Record().set("id", 1).set("long", "x");
//    String rstr = Jsoner.toJSONString(r);
//    System.out.println(rstr);
//    System.out.println(Jsoner.parseObject(rstr, Record.class));


  }
}
