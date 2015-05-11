package cn.dreampie.common.util.json;

import cn.dreampie.common.entity.Entity;
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

  public static Map<String, Object> deserialze(Map<String, Object> map, Class<?> clazz) {
    Object obj = null;
    Method method = null;
    String attrName = null;
    Class<?> returnType = null;

    Entity entity = null;

    List<Map<String, Object>> list = null;
    List<Entity> newlist = null;

    JSONArray blist = null;
    List<?> newblist = null;

    Class returnTypeClass = null;

    Set<Entity> newset = null;

    JSONArray bset = null;
    Set<?> newbset = null;
    for (String key : map.keySet()) {
      obj = map.get(key);
      if (obj != null) {
        try {
          attrName = Stringer.firstUpperCamelCase(key);
          try {
            method = clazz.getDeclaredMethod("get" + attrName);
            returnType = method.getReturnType();
          } catch (NoSuchMethodException getE) {
            try {
              method = clazz.getDeclaredMethod("is" + attrName);
              returnType = method.getReturnType();
            } catch (NoSuchMethodException isE) {
            }
          }
          if (returnType == null) {
            continue;
          }
          //如果已经是返回类型 不再处理
          if (!returnType.isAssignableFrom(obj.getClass()) && !JSON.class.isAssignableFrom(obj.getClass())) {
            //如果是String类型
            if (String.class.isAssignableFrom(returnType)) {
              map.put(key, Jsoner.toJSON(obj));
            } else {
              //判断是不是包含 Entity类型
              if (obj instanceof JSONObject) {
                if (Entity.class.isAssignableFrom(returnType)) {
                  entity = (Entity) returnType.newInstance();
                  entity.putAttrs(deserialze((Map<String, Object>) obj, returnType));
                  map.put(key, entity);
                }
              } else
                //判断是否是Entity的集合类型
                if (obj instanceof JSONArray) {
                  if (Collection.class.isAssignableFrom(returnType)) {
                    returnTypeClass = (Class<?>) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
                    if (List.class.isAssignableFrom(returnType)) {
                      if (Entity.class.isAssignableFrom(returnTypeClass)) {
                        list = (List<Map<String, Object>>) obj;
                        newlist = new ArrayList<Entity>();
                        for (Map<String, Object> mp : list) {
                          entity = (Entity) returnTypeClass.newInstance();
                          entity.putAttrs(deserialze(mp, returnTypeClass));
                          newlist.add(entity);
                        }
                        map.put(key, newlist);
                      } else {
                        blist = (JSONArray) obj;
                        if (String.class == returnTypeClass) {
                          newblist = new ArrayList<String>();
                          for (Object o : blist) {
                            ((List<String>) newblist).add(o.toString());
                          }
                        } else {
                          newblist = new ArrayList<Object>();
                          for (Object o : blist) {
                            if (returnTypeClass.isAssignableFrom(o.getClass()))
                              ((List<Object>) newblist).add(o);
                            else
                              ((List<Object>) newblist).add(Jsoner.toObject(Jsoner.toJSON(o), returnTypeClass));
                          }
                        }
                        map.put(key, newblist);
                      }
                    } else if (Set.class.isAssignableFrom(returnType)) {
                      if (Entity.class.isAssignableFrom(returnTypeClass)) {
                        list = (List<Map<String, Object>>) obj;
                        newset = new HashSet<Entity>();
                        for (Map<String, Object> mp : list) {
                          entity = (Entity) returnTypeClass.newInstance();
                          entity.putAttrs(deserialze(mp, returnTypeClass));
                          newset.add(entity);
                        }
                        map.put(key, newset);
                      } else {
                        bset = (JSONArray) obj;
                        if (String.class.isAssignableFrom(returnTypeClass)) {
                          newbset = new HashSet<String>();
                          for (Object o : bset) {
                            ((Set<String>) newbset).add(o.toString());
                          }
                        } else {
                          newbset = new HashSet<Object>();
                          for (Object o : bset) {
                            if (returnTypeClass.isAssignableFrom(o.getClass()))
                              ((Set<Object>) newbset).add(o);
                            else
                              ((Set<Object>) newbset).add(Jsoner.toObject(Jsoner.toJSON(o), returnTypeClass));
                          }
                        }
                        map.put(key, newbset);
                      }
                    }
                  }
                } else {
                  map.put(key, Jsoner.toObject(Jsoner.toJSON(obj), returnType));
                }
            }
          }
        } catch (Exception e) {
          throw new JSONException("Unconvert type " + returnType, e);
        }
      }
    }
    return map;
  }

  public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
    Map<String, Object> map = parser.parseObject();
    Class<?> clazz = (Class<?>) type;
    if (clazz.isInterface()) {
      throw new JSONException("Unsupport type " + type);
    }

    try {
      Entity e = (Entity) clazz.newInstance();
      if (e.checkMethod()) {
        return (T) e.putAttrs(deserialze(map, clazz));
      } else {
        return (T) e.putAttrs(map);
      }
    } catch (Exception e) {
      throw new JSONException("Unsupport type " + type, e);
    }
  }

  public int getFastMatchToken() {
    return JSONToken.LBRACE;
  }

}
