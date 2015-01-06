package cn.dreampie.config;

import cn.dreampie.common.util.properties.Prop;
import cn.dreampie.common.util.properties.Proper;
import cn.dreampie.orm.ActiveRecordPlugin;
import cn.dreampie.orm.druid.DruidPlugin;
import cn.dreampie.route.config.*;
import cn.dreampie.route.handler.cors.CORSHandler;
import cn.dreampie.route.interceptor.transaction.TransactionInterceptor;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.wall.WallFilter;

/**
 * Created by ice on 14-12-29.
 */
public class AppConfig extends Config {
  public void configConstant(ConstantLoader constantLoader) {
    //通过后缀来返回不同的数据类型  你可以自定义自己的  render  如：FreemarkerRender
    //constantLoader.addRender("json", new JsonRender());
    //启用缓存并在要自动使用缓存的model上  开启缓存@Table(name = "sec_user", cached = true)
    constantLoader.setCacheEnable(true);
  }

  public void configResource(ResourceLoader resourceLoader) {
    //设置resource的目录  减少启动扫描目录
    resourceLoader.addIncludePaths("cn.dreampie");
  }

  public void configPlugin(PluginLoader pluginLoader) {
    Prop prop = Proper.use("application.properties");
    //第一个数据库
    DruidPlugin druidPlugin = new DruidPlugin(prop.get("db.default.url"), prop.get("db.default.user"), prop.get("db.default.password"), prop.get("db.default.driver"), prop.get("db.default.dialect"));
    // StatFilter提供JDBC层的统计信息
    druidPlugin.addFilter(new StatFilter());
    // WallFilter的功能是防御SQL注入攻击
    WallFilter wallDefault = new WallFilter();
    wallDefault.setDbType("mysql");
    druidPlugin.addFilter(wallDefault);

    druidPlugin.setInitialSize(prop.getInt("db.default.poolInitialSize"));
    druidPlugin.setMaxPoolPreparedStatementPerConnectionSize(prop.getInt("db.default.poolMaxSize"));
    druidPlugin.setTimeBetweenConnectErrorMillis(prop.getInt("db.default.connectionTimeoutMillis"));

    pluginLoader.add(druidPlugin);
    ActiveRecordPlugin activeRecordPlugin = new ActiveRecordPlugin(druidPlugin);
    activeRecordPlugin.addIncludePaths("cn.dreampie.example");
    activeRecordPlugin.setShowSql(true);


    pluginLoader.add(activeRecordPlugin);
    //第二个数据库
    DruidPlugin demoPlugin = new DruidPlugin(prop.get("db.demo.url"), prop.get("db.demo.user"), prop.get("db.demo.password"), prop.get("db.demo.driver"), prop.get("db.demo.dialect"));
    // StatFilter提供JDBC层的统计信息
    demoPlugin.addFilter(new StatFilter());
    // WallFilter的功能是防御SQL注入攻击
    WallFilter wallDemo = new WallFilter();
    wallDemo.setDbType("mysql");
    demoPlugin.addFilter(wallDemo);

    demoPlugin.setInitialSize(prop.getInt("db.default.poolInitialSize"));
    demoPlugin.setMaxPoolPreparedStatementPerConnectionSize(prop.getInt("db.default.poolMaxSize"));
    demoPlugin.setTimeBetweenConnectErrorMillis(prop.getInt("db.default.connectionTimeoutMillis"));

    pluginLoader.add(demoPlugin);
    ActiveRecordPlugin demoRecordPlugin = new ActiveRecordPlugin("demo", demoPlugin);
    demoRecordPlugin.addIncludePaths("cn.dreampie.demo");
    demoRecordPlugin.setShowSql(true);
    pluginLoader.add(demoRecordPlugin);
  }

  public void configInterceptor(InterceptorLoader interceptorLoader) {
    //事务的拦截器 @Transaction
    interceptorLoader.add(new TransactionInterceptor());
  }

  public void configHandler(HandlerLoader handlerLoader) {
    //跨域
    handlerLoader.add(new CORSHandler());
  }
}
