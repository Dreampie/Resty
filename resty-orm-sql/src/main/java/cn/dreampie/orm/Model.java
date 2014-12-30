package cn.dreampie.orm;

import java.util.Map;

/**
 * Created by ice on 14-12-30.
 */
public class Model {

  /**
   * Attributes of this model
   */
  private Map<String, Object> attrs = getAttrsMap();

  private Map<String, Object> getAttrsMap() {
    Config config = getConfig();
    if (config == null)
      return DbKit.brokenConfig.containerFactory.getAttrsMap();
    return config.containerFactory.getAttrsMap();
  }
}
