package cn.dreampie.common.util.json;

import cn.dreampie.common.entity.Entity;
import cn.dreampie.common.util.Stringer;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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

  public static Entity deserialze(JSONObject jsonObject, Class<? extends Entity> entityClass) {
    Entity result;
    try {
      result = entityClass.newInstance();
    } catch (Exception e) {
      throw new JSONException("Could not init entity " + entityClass, e);
    }
    Object obj = null;
    Method method = null;
    String attrName = null;
    Class<?> returnType = null;

    List<JSONObject> list = null;
    List<Entity> newlist = null;

    JSONArray blist = null;
    List<?> newblist = null;

    Class returnTypeClass = null;

    Set<Entity> newset = null;

    JSONArray bset = null;
    Set<?> newbset = null;
    for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
      obj = entry.getValue();
      if (obj != null) {
        returnType = null;
        try {
          if (!result.hasColumn(entry.getKey())) {
            attrName = Stringer.firstUpperCamelCase(entry.getKey());
            try {
              method = entityClass.getDeclaredMethod("get" + attrName);
              returnType = method.getReturnType();
            } catch (NoSuchMethodException getE) {
              try {
                method = entityClass.getDeclaredMethod("is" + attrName);
                returnType = method.getReturnType();
              } catch (NoSuchMethodException isE) {
              }
            }
          } else {
            returnType = result.getColumnType(entry.getKey());
          }
          if (returnType == null) {
            result.put(entry.getKey(), entry.getValue());
            continue;
          }
          //如果是String类型
          if (String.class.isAssignableFrom(returnType)) {
            result.put(entry.getKey(), obj.toString());
          } else {
            //判断是不是包含 Entity类型
            if (obj instanceof JSONObject) {
              if (Entity.class.isAssignableFrom(returnType)) {
                result.put(entry.getKey(), deserialze((JSONObject) obj, (Class<? extends Entity>) returnType));
              } else {
                result.put(entry.getKey(), parse(obj, returnType));
              }
            } else
              //判断是否是Entity的集合类型
              if (obj instanceof JSONArray) {
                if (Collection.class.isAssignableFrom(returnType)) {
                  returnTypeClass = (Class<?>) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
                  if (List.class.isAssignableFrom(returnType)) {
                    if (Entity.class.isAssignableFrom(returnTypeClass)) {
                      list = (List<JSONObject>) obj;
                      newlist = new ArrayList<Entity>();
                      for (JSONObject jo : list) {
                        newlist.add(deserialze(jo, (Class<? extends Entity>) returnTypeClass));
                      }
                      result.put(entry.getKey(), newlist);
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
                            ((List<Object>) newblist).add(parse(obj, returnTypeClass));
                        }
                      }
                      result.put(entry.getKey(), newblist);
                    }
                  } else if (Set.class.isAssignableFrom(returnType)) {
                    if (Entity.class.isAssignableFrom(returnTypeClass)) {
                      list = (List<JSONObject>) obj;
                      newset = new HashSet<Entity>();
                      for (JSONObject jo : list) {
                        newset.add(deserialze(jo, (Class<? extends Entity>) returnTypeClass));
                      }
                      result.put(entry.getKey(), newset);
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
                            ((Set<Object>) newbset).add(parse(o, returnTypeClass));
                        }
                      }
                      result.put(entry.getKey(), newbset);
                    }
                  }
                } else {
                  result.put(entry.getKey(), parse(obj, returnType));
                }
              } else {
                result.put(entry.getKey(), parse(obj, returnType));
              }
          }
        } catch (Exception e) {
          throw new JSONException("Unconvert type " + returnType, e);
        }
      }
    }

    return result;
  }

  /**
   * 转换非集合对象
   *
   * @param obj
   * @param paramType
   * @return
   */
  public static Object parse(Object obj, Class paramType) throws IllegalAccessException, InstantiationException {
    Object result = null;
    if (obj != null) {
      if (paramType.isAssignableFrom(obj.getClass())) {
        return obj;
      } else {
        if (obj instanceof JSONObject && Entity.class.isAssignableFrom(paramType)) {
          result = deserialze((JSONObject) obj, (Class<? extends Entity>) paramType);
        } else if (paramType == String.class) {
          result = obj;
        } else {
          if (obj instanceof String && (((String) obj).startsWith("\"") || ((String) obj).startsWith("{") || ((String) obj).startsWith("["))) {
            result = Jsoner.toObject((String) obj, paramType);
          } else {
            result = Jsoner.toObject(Jsoner.toJSON(obj), paramType);
          }
        }
      }
    }
    return result;
  }

  public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
    JSONObject jsonObject = parser.parseObject();
    Class<?> clazz = (Class<?>) type;
    if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
      throw new JSONException("Unsupport type " + type);
    }

    try {
      Entity e = (Entity) clazz.newInstance();
      if (e.checkMethod()) {
        return (T) e.putAttrs(deserialze(jsonObject, (Class<? extends Entity>) clazz));
      } else {
        return (T) e.putAttrs(jsonObject);
      }
    } catch (Exception e) {
      throw new JSONException("Unsupport type " + type, e);
    }
  }

  public int getFastMatchToken() {
    return JSONToken.LBRACE;
  }

}
