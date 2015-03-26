package cn.dreampie;


import cn.dreampie.orm.ActiveRecordPlugin;
import cn.dreampie.orm.Record;
import cn.dreampie.orm.provider.druid.DruidDataSourceProvider;
import org.junit.Test;

/**
 * Unit test for activeRecord plugin.
 */
public class ActiveRecordPluginTest {
  @Test
  public void testStart() {
    ActiveRecordPlugin activeRecordPlugin = new ActiveRecordPlugin(new DruidDataSourceProvider("default"));
//    activeRecordPlugin.addIncludePaths("cn.dremapie.orm");
    activeRecordPlugin.start();

    Record recordDAO = Record.use("ds2","test");

    recordDAO.reNew().set("id2", 10).save();

    recordDAO = Record.use("test").findColsByIds("id,id2", new Object[]{recordDAO.get("id"), recordDAO.get("id2")});

    recordDAO.update();
    //Record 解析支持
//    Jsoner.addConfig(Record.class, ModelSerializer.instance(), ModelDeserializer.instance());
//    Record r = new Record().set("id", 1).set("long", "x");
//    String rstr = Jsoner.toJSONString(r);
//    System.out.println(rstr);
//    System.out.println(Jsoner.parseObject(rstr, Record.class));


  }
}
