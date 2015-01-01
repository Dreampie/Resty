package cn.dreampie.util.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;

import java.lang.reflect.Type;

/**
 * Created by ice on 14-12-31.
 */
public class Jsoner {
  private static SerializeConfig config = SerializeConfig.getGlobalInstance();
  private static ParserConfig parserConfig = ParserConfig.getGlobalInstance();

  public static void addConfig(Type type, ObjectSerializer serializer, ObjectDeserializer deserializer) {
    addSerializer(type, serializer);
    addDeserializer(type, deserializer);
  }

  public static void addSerializer(Type type, ObjectSerializer serializer) {
    config.put(type, serializer);
  }

  public static void addDeserializer(Type type, ObjectDeserializer deserializer) {
    parserConfig.putDeserializer(type, deserializer);
  }

  public static String toJSONString(Object object) {
    return JSON.toJSONString(object, config);
  }

  public static <T> T parseObject(String json, Class<T> clazz) {
    return JSON.parseObject(json, clazz);
  }
}
