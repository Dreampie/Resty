package cn.dreampie.example;

import cn.dreampie.core.route.annotation.GET;
import cn.dreampie.core.route.annotation.POST;
import cn.dreampie.core.route.base.Resource;
import cn.dreampie.kit.Lister;
import cn.dreampie.kit.Maper;

import java.util.List;
import java.util.Map;

/**
 * Created by ice on 14-12-29.
 */
public class DemoResource extends Resource {

  @GET("/demos/:name")
  public Map find(String name) {
    return Maper.of("k1", "v1,name:" + name, "k2", "v2");
  }

  @POST("/demos")
  public List add(String o1, String o2) {
    return Lister.of(o1, o2);
  }
}
