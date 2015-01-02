package cn.dreampie.example.config;

import cn.dreampie.orm.ActiveRecordPlugin;
import cn.dreampie.orm.druid.DruidPlugin;
import cn.dreampie.route.config.*;
import cn.dreampie.route.render.JsonRender;
import cn.dreampie.util.properties.Prop;
import cn.dreampie.util.properties.Proper;

/**
 * Created by ice on 14-12-29.
 */
public class AppConfig extends Config {
  public void configConstant(ConstantLoader constantLoader) {
//    constantLoader.addRender("json", new JsonRender());
  }

  public void configResource(ResourceLoader resourceLoader) {

  }

  public void configPlugin(PluginLoader pluginLoader) {
    Prop prop = Proper.use("application.properties");
    DruidPlugin druidPlugin = new DruidPlugin(prop.get("db.default.url"), prop.get("db.default.user"), prop.get("db.default.password"), prop.get("db.default.driver"), prop.get("db.default.dialect"));
    pluginLoader.add(druidPlugin);
    ActiveRecordPlugin activeRecordPlugin = new ActiveRecordPlugin(druidPlugin);
    activeRecordPlugin.addIncludePaths("cn.dreampie.example");
    activeRecordPlugin.setShowSql(true);
    pluginLoader.add(activeRecordPlugin);
  }

  public void configInterceptor(InterceptorLoader interceptorLoader) {

  }

  public void configHandler(HandlerLoader handlerLoader) {

  }
}
