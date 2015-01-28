package cn.dreampie.common.util.json;

import cn.dreampie.common.Entity;
import cn.dreampie.common.util.Stringer;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by ice on 14-12-31.
 */
public enum ModelDeserializer implements ObjectDeserializer {
  INSTANCE;

  public static ModelDeserializer instance() {
    return INSTANCE;
  }


  public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {

    Map<String, Object> map = parser.parseObject();

    Class<?> clazz = (Class<?>) type;
    if (clazz.isInterface()) {
      throw new JSONException("Unsupport type " + type);
    }


    try {
      Entity<T> e = (Entity<T>) clazz.newInstance();
      return e.putAttrs(deserialze(map, clazz));
    } catch (Exception e) {
      throw new JSONException("Unsupport type " + type, e);
    }
  }

  public int getFastMatchToken() {
    return JSONToken.LBRACE;
  }

  public static Map<String, Object> deserialze(Map<String, Object> map, Class<?> clazz) {
    Object obj = null;
    Method method = null;
    Class<?> returnType = null;

    List<Map<String, Object>> list = null;
    List<Entity<?>> newlist = null;

    JSONArray blist = null;
    List<?> newblist = null;

    Class returnTypeClass = null;

    Set<Entity<?>> newset = null;

    JSONArray bset = null;
    Set<?> newbset = null;
    for (String key : map.keySet()) {
      obj = map.get(key);
      try {
        method = clazz.getDeclaredMethod("get" + Stringer.firstUpperCamelCase(key));
        returnType = method.getReturnType();
        //判断是不是包含 Entity类型
        if (obj instanceof JSONObject) {
          if (Entity.class.isAssignableFrom(returnType)) {
            Entity<?> e = (Entity<?>) returnType.newInstance();
            e.putAttrs(deserialze((Map<String, Object>) obj, returnType));
            map.put(key, e);
          }
        } else
          //判断是否是Entity的集合类型
          if (obj instanceof JSONArray) {
            if (Collection.class.isAssignableFrom(returnType)) {
              returnTypeClass = (Class<?>) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
              if (List.class.isAssignableFrom(returnType)) {
                if (Entity.class.isAssignableFrom(returnTypeClass)) {
                  list = (List<Map<String, Object>>) obj;
                  newlist = new ArrayList<Entity<?>>();
                  for (Map<String, Object> mp : list) {
                    Entity<?> e = (Entity<?>) returnTypeClass.newInstance();
                    e.putAttrs(deserialze(mp, returnTypeClass));
                    newlist.add(e);
                  }
                  map.put(key, newlist);
                } else {
                  blist = (JSONArray) obj;
                  if (String.class.isAssignableFrom(returnTypeClass)) {
                    newblist = new ArrayList<String>();
                    for (Object e : blist) {
                      ((List<String>) newblist).add(e.toString());
                    }
                  } else {
                    newblist = new ArrayList<Object>();
                    for (Object e : blist) {
                      if (e.getClass().isAssignableFrom(returnTypeClass))
                        ((List<Object>) newblist).add(e);
                      else
                        ((List<Object>) newblist).add(JSON.parseObject(JSON.toJSONString(e), returnTypeClass));
                    }
                  }
                  map.put(key, newblist);
                }
              } else if (Set.class.isAssignableFrom(returnType)) {
                if (Entity.class.isAssignableFrom(returnTypeClass)) {
                  list = (List<Map<String, Object>>) obj;
                  newset = new HashSet<Entity<?>>();
                  for (Map<String, Object> mp : list) {
                    Entity<?> e = (Entity<?>) returnTypeClass.newInstance();
                    e.putAttrs(deserialze(mp, returnTypeClass));
                    newset.add(e);
                  }
                  map.put(key, newset);
                } else {
                  bset = (JSONArray) obj;
                  if (String.class.isAssignableFrom(returnTypeClass)) {
                    newbset = new HashSet<String>();
                    for (Object e : bset) {
                      ((Set<String>) newbset).add(e.toString());
                    }
                  } else {
                    newbset = new HashSet<Object>();
                    for (Object e : bset) {
                      if (e.getClass().isAssignableFrom(returnTypeClass))
                        ((Set<Object>) newbset).add(e);
                      else
                        ((Set<Object>) newbset).add(JSON.parseObject(JSON.toJSONString(e), returnTypeClass));
                    }
                  }
                  map.put(key, newbset);
                }
              }
            }
          } else {
            if (String.class.isAssignableFrom(returnType))
              map.put(key, obj.toString());
            else {
              if (!obj.getClass().isAssignableFrom(returnType))
                map.put(key, JSON.parseObject(JSON.toJSONString(obj), returnType));
            }
          }
      } catch (NoSuchMethodException e) {
      } catch (Exception e) {
        throw new JSONException("Unconvert type " + returnType, e);
      }
    }
    return map;
  }

}
