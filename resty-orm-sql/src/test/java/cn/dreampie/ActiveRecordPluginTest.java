package cn.dreampie;


import cn.dreampie.common.util.properties.Prop;
import cn.dreampie.common.util.properties.Proper;
import cn.dreampie.orm.ActiveRecordPlugin;
import cn.dreampie.orm.DS;
import cn.dreampie.orm.Record;
import cn.dreampie.orm.druid.DruidPlugin;
import org.junit.Test;

/**
 * Unit test for activeRecord plugin.
 */
public class ActiveRecordPluginTest {
  @Test
  public void testStart() {
    Prop prop = Proper.use("application.properties");
    DruidPlugin druidPlugin = new DruidPlugin("default");
    druidPlugin.start();
    ActiveRecordPlugin activeRecordPlugin = new ActiveRecordPlugin(druidPlugin);
    activeRecordPlugin.addIncludePaths("cn.dremapie.orm");
    activeRecordPlugin.start();

    Record record = new Record().set("username", "test").set("password", "123").set("sid", 2).set("providername", "a");

    DS.use().save("sec_user", record);

    record = DS.use().findById("sec_user", record.get("id"));

    DS.use().update("sec_user", record);
  }
}
