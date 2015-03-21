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
    DruidPlugin druidPlugin = new DruidPlugin("default");
    druidPlugin.start();
    ActiveRecordPlugin activeRecordPlugin = new ActiveRecordPlugin("default", druidPlugin);
    activeRecordPlugin.addIncludePaths("cn.dreampie.resource");
    activeRecordPlugin.start();

  }
}
