package cn.dreampie.example;

import cn.dreampie.orm.ActiveRecordPlugin;
import cn.dreampie.orm.druid.DruidPlugin;
import cn.dreampie.util.properties.Prop;
import cn.dreampie.util.properties.Proper;

/**
 * Created by wangrenhui on 15/1/1.
 */
public class ActiveRecord {
  public static void init() {
    Prop prop = Proper.use("application.properties");
    DruidPlugin druidPlugin = new DruidPlugin(prop.get("db.default.url"), prop.get("db.default.user"), prop.get("db.default.password"), prop.get("db.default.driver"), prop.get("db.default.dialect"));
    druidPlugin.start();
    ActiveRecordPlugin activeRecordPlugin = new ActiveRecordPlugin(druidPlugin);
    activeRecordPlugin.addIncludePaths("cn.dreampie.example");
    activeRecordPlugin.start();
  }
}
