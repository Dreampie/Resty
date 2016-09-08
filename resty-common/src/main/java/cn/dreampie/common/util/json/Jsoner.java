package cn.dreampie.common.util.json;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.parser.deserializer.ParseProcess;
import com.alibaba.fastjson.serializer.*;

import java.lang.reflect.Type;

/**
 * Created by ice on 14-12-31.
 */
public class Jsoner {
  private static SerializeConfig config = SerializeConfig.getGlobalInstance();
  private static ParserConfig parserConfig = ParserConfig.getGlobalInstance();
  private static SerializerFeature[] serializerFeatures;
  private static Feature[] deserializerFeatures;

  static {
    config.put(java.util.Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
    config.put(java.sql.Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd"));
    config.put(java.sql.Timestamp.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss.SSS"));
    config.put(java.sql.Time.class, new SimpleDateFormatSerializer("HH:mm:ss"));
  }

  public static void addConfig(Type type, ObjectSerializer serializer, ObjectDeserializer deserializer) {
    addSerializer(type, serializer);
    addDeserializer(type, deserializer);
  }

  public static void addSerializerFeature(SerializerFeature... features) {
    serializerFeatures = features;
  }

  public static void addDeserializerFeature(Feature... features) {
    deserializerFeatures = features;
  }

  public static void addSerializer(Type type, ObjectSerializer serializer) {
    config.put(type, serializer);
  }

  public static void addDeserializer(Type type, ObjectDeserializer deserializer) {
    parserConfig.putDeserializer(type, deserializer);
  }

  public static String toJSON(Object object) {
    if (serializerFeatures != null) {
      return JSON.toJSONString(object, serializerFeatures);
    } else {
      return JSON.toJSONString(object);
    }
  }

  public static String toJSON(Object object, SerializerFeature... features) {
    return JSON.toJSONString(object, features);
  }

  public static String toJSON(Object object, SerializeFilter[] filters) {
    if (serializerFeatures != null) {
      return JSON.toJSONString(object, filters, serializerFeatures);
    } else {
      return JSON.toJSONString(object, filters);
    }
  }

  public static String toJSON(Object object, SerializeFilter[] filters, SerializerFeature... features) {
    return JSON.toJSONString(object, filters, features);
  }

  public static String toJSON(Object object, SerializeConfig config) {
    if (serializerFeatures != null) {
      return JSON.toJSONString(object, config, serializerFeatures);
    } else {
      return JSON.toJSONString(object, config);
    }
  }

  public static String toJSON(Object object, SerializeConfig config, SerializerFeature... features) {
    return JSON.toJSONString(object, config, features);
  }

  public static String toJSON(Object object, SerializeConfig config, SerializeFilter filter) {
    if (serializerFeatures != null) {
      return JSON.toJSONString(object, config, filter, serializerFeatures);
    } else {
      return JSON.toJSONString(object, config, filter);
    }
  }

  public static String toJSON(Object object, SerializeConfig config, SerializeFilter filter, SerializerFeature... features) {
    return JSON.toJSONString(object, config, filter, features);
  }

  public static <T> T toObject(String json) {
    try {
      return (T) JSON.parse(json);
    } catch (JSONException e) {
      throw new JsonException("Could not cast \"" + json + "\" to " + JSONObject.class.getName(), e);
    }
  }

  public static <T> T toObject(String json, Class<T> clazz) {
    try {
      if (deserializerFeatures != null) {
        return JSON.parseObject(json, clazz, deserializerFeatures);
      } else {
        return JSON.parseObject(json, clazz);
      }
    } catch (JSONException e) {
      throw new JsonException("Could not cast \"" + json + "\" to " + clazz.getName(), e);
    }
  }

  public static <T> T toObject(String json, Class<T> clazz, Feature... features) {
    try {
      return JSON.parseObject(json, clazz, features);
    } catch (JSONException e) {
      throw new JsonException("Could not cast \"" + json + "\" to " + clazz.getName(), e);
    }
  }

  public static <T> T toObject(String json, Class<T> clazz, ParseProcess processor) {
    try {
      if (deserializerFeatures != null) {
        return JSON.parseObject(json, clazz, processor, deserializerFeatures);
      } else {
        return JSON.parseObject(json, clazz, processor);
      }
    } catch (JSONException e) {
      throw new JsonException("Could not cast \"" + json + "\" to " + clazz.getName(), e);
    }
  }


  public static <T> T toObject(String json, Class<T> clazz, ParseProcess processor, Feature... features) {
    try {
      return JSON.parseObject(json, clazz, processor, features);
    } catch (JSONException e) {
      throw new JsonException("Could not cast \"" + json + "\" to " + clazz.getName(), e);
    }
  }

  public static <T> T toObject(String json, TypeReference<T> type, Feature... features) {
    try {
      return JSON.parseObject(json, type, features);
    } catch (JSONException e) {
      throw new JsonException("Could not cast \"" + json + "\" to " + type.getClass().getName(), e);
    }
  }

  public static <T> T toObject(String json, Type type) {
    try {
      if (deserializerFeatures != null) {
        return JSON.parseObject(json, type, deserializerFeatures);
      } else {
        return JSON.parseObject(json, type);
      }
    } catch (JSONException e) {
      throw new JsonException("Could not cast \"" + json + "\" to " + type.getClass().getName(), e);
    }
  }

  public static <T> T toObject(String json, Type type, Feature... features) {
    try {
      return JSON.parseObject(json, type, features);
    } catch (JSONException e) {
      throw new JsonException("Could not cast \"" + json + "\" to " + type.getClass().getName(), e);
    }
  }

  public static <T> T toObject(String json, Type type, ParseProcess processor) {
    try {
      if (deserializerFeatures != null) {
        return JSON.parseObject(json, type, processor, deserializerFeatures);
      } else {
        return JSON.parseObject(json, type, processor);
      }
    } catch (JSONException e) {
      throw new JsonException("Could not cast \"" + json + "\" to " + type.getClass().getName(), e);
    }
  }

  public static <T> T toObject(String json, Type type, ParseProcess processor, Feature... features) {
    try {
      return JSON.parseObject(json, type, processor, features);
    } catch (JSONException e) {
      throw new JsonException("Could not cast \"" + json + "\" to " + type.getClass().getName(), e);
    }
  }

  public static boolean isJson(String source) {
    boolean result = false;
    if (source != null) {
      result = source.startsWith("\"") || source.startsWith("{") || source.startsWith("[");
      if (!result) {
        try {
          result = JSON.parse(source) != null;
        } catch (JSONException e) {
          result = false;
        }
      }
    }
    return result;
  }

  public static void clear() {
    serializerFeatures = null;
    deserializerFeatures = null;
  }
}
