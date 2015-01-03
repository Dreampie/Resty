package cn.dreampie;


import cn.dreampie.orm.ActiveRecordPlugin;
import cn.dreampie.orm.DS;
import cn.dreampie.orm.Record;
import cn.dreampie.orm.druid.DruidPlugin;
import cn.dreampie.util.properties.Prop;
import cn.dreampie.util.properties.Proper;
import org.junit.Test;

/**
 * Unit test for activeRecord plugin.
 */
public class ActiveRecordPluginTest {
  @Test
  public void testStart() {
    Prop prop = Proper.use("application.properties");
    DruidPlugin druidPlugin = new DruidPlugin(prop.get("db.default.url"), prop.get("db.default.user"), prop.get("db.default.password"), prop.get("db.default.driver"), prop.get("db.default.dialect"));
    druidPlugin.start();
    ActiveRecordPlugin activeRecordPlugin = new ActiveRecordPlugin(druidPlugin);
    activeRecordPlugin.addIncludePaths("cn.dremapie.orm");
    activeRecordPlugin.start();

    Record record = DS.use().findById("sec_user", 1);

  }
}
