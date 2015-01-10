package cn.dreampie;

import cn.dreampie.common.util.Maper;
import cn.dreampie.route.core.annotation.DELETE;
import cn.dreampie.route.core.annotation.GET;
import cn.dreampie.route.core.annotation.POST;
import cn.dreampie.route.core.annotation.PUT;

import java.util.Map;

/**
 * Created by wangrenhui on 15/1/10.
 */
public class TestResource extends ApiResource {

  @GET("/tests")
  public Map get() {
    return Maper.of("a", "1", "b", "2");
  }

  @POST("/tests")
  public Map post(Map<String, String> test) {
    return test;
  }

  @PUT("/tests/:b")
  public Map put(String b) {
    Map<String, String> map = Maper.of("a", "1", "b", "2");
    map.put("b", b);
    return map;
  }

  @DELETE("/tests/:key")
  public Map delete(String key) {
    Map<String, String> map = Maper.of("a", "1", "b", "2");
    map.remove(key);
    return map;
  }

}
