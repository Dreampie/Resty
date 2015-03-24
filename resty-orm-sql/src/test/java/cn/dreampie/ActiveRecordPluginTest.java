package cn.dreampie;


import cn.dreampie.common.entity.Record;
import cn.dreampie.common.util.json.Jsoner;
import cn.dreampie.common.util.json.ModelDeserializer;
import cn.dreampie.common.util.json.ModelSerializer;
import cn.dreampie.orm.ActiveRecordPlugin;
import cn.dreampie.orm.DS;
import cn.dreampie.orm.Model;
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

    Record record = new Record().set("id2", 10);

    DS.use().save("test", record);

    record = DS.use().findColsByIds("test","id,id2", new Object[]{record.get("id"),record.get("id2")});

    DS.use().update("sec_user", record);
    //Record 解析支持
//    Jsoner.addConfig(Record.class, ModelSerializer.instance(), ModelDeserializer.instance());
//    Record r = new Record().set("id", 1).set("long", "x");
//    String rstr = Jsoner.toJSONString(r);
//    System.out.println(rstr);
//    System.out.println(Jsoner.parseObject(rstr, Record.class));


  }
}
