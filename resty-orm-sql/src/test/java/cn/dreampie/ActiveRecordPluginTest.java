package cn.dreampie;


import cn.dreampie.orm.ActiveRecordPlugin;
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
    new ActiveRecordPlugin(new DruidPlugin(prop.get("db.default.url"), prop.get("db.default.user"), prop.get("db.default.password"), prop.get("db.default.driver"), prop.get("db.default.dialect"))).start();
  }
}
