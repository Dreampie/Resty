package cn.dreampie.example;

import cn.dreampie.common.util.properties.Prop;
import cn.dreampie.common.util.properties.Proper;
import cn.dreampie.orm.ActiveRecordPlugin;
import cn.dreampie.orm.druid.DruidPlugin;

/**
 * Created by wangrenhui on 15/1/1.
 */
public class ActiveRecord {
  public static void init() {
    Prop prop = Proper.use("application.properties");
    //第一个数据库
    DruidPlugin druidPlugin = new DruidPlugin(prop.get("db.default.url"), prop.get("db.default.user"), prop.get("db.default.password"), prop.get("db.default.driver"), prop.get("db.default.dialect"));
    druidPlugin.start();
    ActiveRecordPlugin activeRecordPlugin = new ActiveRecordPlugin("default", druidPlugin);
    activeRecordPlugin.addIncludePaths("cn.dreampie.example");
    activeRecordPlugin.setShowSql(true);
    activeRecordPlugin.start();
    //第二个数据库
    DruidPlugin demoPlugin = new DruidPlugin(prop.get("db.demo.url"), prop.get("db.demo.user"), prop.get("db.demo.password"), prop.get("db.demo.driver"), prop.get("db.demo.dialect"));
    demoPlugin.start();
    ActiveRecordPlugin demoRecordPlugin = new ActiveRecordPlugin("demo", demoPlugin);
    demoRecordPlugin.addIncludePaths("cn.dreampie.demo");
    demoRecordPlugin.setShowSql(true);
    demoRecordPlugin.start();
  }
}
