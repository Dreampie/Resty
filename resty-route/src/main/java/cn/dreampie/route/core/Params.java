package cn.dreampie.route.core;

import java.util.Collection;
import java.util.LinkedHashMap;
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
    this.params = new LinkedHashMap<String, Object>();
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
}
