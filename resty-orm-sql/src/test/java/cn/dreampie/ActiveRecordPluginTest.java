package cn.dreampie;


import cn.dreampie.common.entity.Record;
import cn.dreampie.common.util.json.Jsoner;
import cn.dreampie.common.util.json.ModelDeserializer;
import cn.dreampie.common.util.json.ModelSerializer;
import org.junit.Test;

/**
 * Unit test for activeRecord plugin.
 */
public class ActiveRecordPluginTest {
  @Test
  public void testStart() {
//    ActiveRecordPlugin activeRecordPlugin = new ActiveRecordPlugin(new DruidDataSourceProvider("default"));
//    activeRecordPlugin.addIncludePaths("cn.dremapie.orm");
//    activeRecordPlugin.start();
//
//    Record record = new Record().set("username", "test").set("password", "123").set("sid", 2).set("providername", "a");
//
//    DS.use().save("sec_user", record);
//
//    record = DS.use().findById("sec_user", record.get("id"));
//
//    DS.use().update("sec_user", record);
    //Record 解析支持
    Jsoner.addConfig(Record.class, ModelSerializer.instance(), ModelDeserializer.instance());
    Record r = new Record().set("id", 1).set("long", "x");
    String rstr = Jsoner.toJSONString(r);
    System.out.println(rstr);
    System.out.println(Jsoner.parseObject(rstr, Record.class));
  }
}
