package cn.dreampie.example;

import cn.dreampie.orm.ActiveRecordPlugin;
import cn.dreampie.orm.provider.druid.DruidDataSourceProvider;

/**
 * Created by wangrenhui on 15/1/1.
 */
public class ActiveRecord {
  public static void init() {
    //第一个数据库
    ActiveRecordPlugin activeRecordPlugin = new ActiveRecordPlugin(new DruidDataSourceProvider("default"));
    activeRecordPlugin.addIncludePackages("cn.dreampie.resource");
    activeRecordPlugin.start();

  }
}
