package cn.dreampie.config;

import cn.dreampie.common.spring.SpringPlugin;
import cn.dreampie.orm.ActiveRecordPlugin;
import cn.dreampie.orm.provider.c3p0.C3p0DataSourceProvider;
import cn.dreampie.orm.provider.druid.DruidDataSourceProvider;
import cn.dreampie.route.config.*;
import cn.dreampie.route.handler.cors.CORSHandler;
import cn.dreampie.route.interceptor.security.SecurityInterceptor;
import cn.dreampie.route.interceptor.transaction.TransactionInterceptor;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * Created by ice on 14-12-29.
 */
public class AppConfig extends Config {

  public void configConstant(ConstantLoader constantLoader) {
    //通过后缀来返回不同的数据类型  你可以自定义自己的  render  如：FreemarkerRender
    //constantLoader.addRender("json", new JsonRender());
    constantLoader.addJsonSerializerFeature(SerializerFeature.DisableCircularReferenceDetect);
  }

  public void configResource(ResourceLoader resourceLoader) {
    //设置resource的目录  减少启动扫描目录
//    resourceLoader.addExcludePackages("cn.dreampie.resource");
    resourceLoader.addIncludePackages("cn.dreampie.resource");
  }

  public void configPlugin(PluginLoader pluginLoader) {
    //第一个数据库
    C3p0DataSourceProvider cdsp = new C3p0DataSourceProvider("default");
    ActiveRecordPlugin activeRecordCdsp = new ActiveRecordPlugin(cdsp);
    activeRecordCdsp.addIncludePackages("cn.dreampie.resource");
    pluginLoader.add(activeRecordCdsp);

    //第二个数据库
    DruidDataSourceProvider ddsp = new DruidDataSourceProvider("demo");
    ActiveRecordPlugin activeRecordDdsp = new ActiveRecordPlugin(ddsp);
    pluginLoader.add(activeRecordDdsp);

    pluginLoader.add(new SpringPlugin(HelloApp.class));
//    JndiDataSourceProvider jdsp = new JndiDataSourceProvider("jndiDs", "jndiName");
//    ActiveRecordPlugin activeRecordJdsp = new ActiveRecordPlugin(ddsp, true);
//    pluginLoader.add(activeRecordJdsp);
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
