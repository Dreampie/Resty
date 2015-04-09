package cn.dreampie.config;

import cn.dreampie.common.util.properties.Prop;
import cn.dreampie.common.util.properties.Proper;
import cn.dreampie.orm.ActiveRecordPlugin;
import cn.dreampie.orm.provider.c3p0.C3p0DataSourceProvider;
import cn.dreampie.orm.provider.druid.DruidDataSourceProvider;
import cn.dreampie.route.config.*;
import cn.dreampie.route.handler.cors.CORSHandler;
import cn.dreampie.route.interceptor.security.SecurityInterceptor;
import cn.dreampie.route.interceptor.transaction.TransactionInterceptor;

/**
 * Created by ice on 14-12-29.
 */
public class AppConfig extends Config {
  private static Prop prop = Proper.use("application.properties");

  public void configConstant(ConstantLoader constantLoader) {
    //通过后缀来返回不同的数据类型  你可以自定义自己的  render  如：FreemarkerRender
    //constantLoader.addRender("json", new JsonRender());

    //以下配置移植到application.properties
    //启用缓存并在要自动使用缓存的model上  开启缓存@Table(name = "sec_user", cached = true)
//    boolean devMode = prop.getBoolean("devMode", false);
//    constantLoader.setCacheEnable(!devMode);//开发模式下不开启缓存
//    constantLoader.setDevMode(devMode);
//    constantLoader.setShowRoute(devMode);//请求时打印route信息

  }

  public void configResource(ResourceLoader resourceLoader) {
    //设置resource的目录  减少启动扫描目录
    resourceLoader.addExcludePaths("cn.dreampie.resource");
    resourceLoader.addIncludePaths("cn.dreampie.resource");
  }

  public void configPlugin(PluginLoader pluginLoader) {
    //第一个数据库
    C3p0DataSourceProvider ddsp = new C3p0DataSourceProvider("default");
    ActiveRecordPlugin activeRecordPlugin = new ActiveRecordPlugin(ddsp, true);
    activeRecordPlugin.addIncludePaths("cn.dreampie.resource");
    pluginLoader.add(activeRecordPlugin);

    //第二个数据库
    DruidDataSourceProvider ddsp2 = new DruidDataSourceProvider("demo");
    ActiveRecordPlugin activeRecordPlugin2 = new ActiveRecordPlugin(ddsp2, true);
    pluginLoader.add(activeRecordPlugin2);
  }

  public void configInterceptor(InterceptorLoader interceptorLoader) {
    //权限拦截器
    interceptorLoader.add(new SecurityInterceptor(2, new MyAuthenticateService()));
    //事务的拦截器 @Transaction
    interceptorLoader.add(new TransactionInterceptor());
  }

  public void configHandler(HandlerLoader handlerLoader) {
    //跨域
    handlerLoader.add(new CORSHandler());
  }
}
