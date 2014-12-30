package cn.dreampie.orm;

import java.util.Map;

/**
 * Created by ice on 14-12-30.
 */
public class Model {

  /**
   * Attributes of this model
   */
  private Map<String, Object> attrs = new CaseInsensitiveMap<Object>();

  public Map<String, Object> getAttrs() {
    return attrs;
  }
}
