package cn.dreampie.route.core;

import cn.dreampie.common.entity.CaseInsensitiveMap;
import cn.dreampie.common.entity.Entity;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by ice on 15-1-26.
 */
public class Params {
  /**
   * params for validate
   */
  private Map<String, Object> params;

  public Params() {
    this.params = new CaseInsensitiveMap<Object>();
  }

  public Params(Entity entity) {
    this.params = entity.getAttrs();
  }

  /**
   * Put key value pair to the params.
   */
  public Params set(String key, Object value) {
    params.put(key, value);
    return this;
  }

  /**
   * Get param of any type.
   */
  public <T> T get(String name) {
    return (T) (params.get(name));
  }

  /**
   * Return param name of this route.
   */
  public String[] getNames() {
    Set<String> nameSet = params.keySet();
    return nameSet.toArray(new String[nameSet.size()]);
  }

  /**
   * Return param values of this route.
   */
  public Object[] getValues() {
    Collection<Object> valueCollection = params.values();
    return valueCollection.toArray(new Object[valueCollection.size()]);
  }

  /**
   * 判断是否存在某个参数
   *
   * @param name
   * @return
   */
  public boolean containsName(String name) {
    return params.containsKey(name);
  }

  /**
   * 判断是否存在某个值
   *
   * @param value
   * @return
   */
  public boolean containsValue(Object value) {
    return params.containsValue(value);
  }
}
