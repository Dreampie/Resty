package cn.dreampie.util.json;

import cn.dreampie.common.Entity;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by ice on 14-12-31.
 */
public enum ModelDeserializer implements ObjectDeserializer {
  INSTANCE;

  public static ModelDeserializer instance() {
    return INSTANCE;
  }


  public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {

    Map<String, Object> json = parser.parseObject();

    Class<?> clazz = (Class<?>) type;
    if (clazz.isInterface()) {
      throw new JSONException("unsupport type " + type);
    }


    try {
      Entity<T> e = (Entity<T>) clazz.newInstance();
      return e.putAttrs(json);
    } catch (Exception e) {
      throw new JSONException("unsupport type " + type, e);
    }
  }

  public int getFastMatchToken() {
    return 0;
  }
}
