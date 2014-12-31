package cn.dreampie.util.json;

import cn.dreampie.common.Entity;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializeConfig;

/**
 * Created by ice on 14-12-31.
 */
public class Jsoner {
  private static SerializeConfig config;
  private static ParserConfig parserConfig;

  public static String toJSONString(Object object) {
    if (config == null) {
      config = SerializeConfig.getGlobalInstance();
      config.put(Entity.class, new ModelSerializer());
    }
    return JSON.toJSONString(object, config);
  }

  public static Object parseObject(String json, Class<?> clazz) {
    if (parserConfig == null) {
      parserConfig = ParserConfig.getGlobalInstance();
      parserConfig.putDeserializer(Entity.class, new ModelDeserializer());
    }
    return JSON.parseObject(json, clazz);
  }
}
