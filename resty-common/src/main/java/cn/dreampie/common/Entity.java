package cn.dreampie.common;

import java.util.Map;

/**
 * Created by ice on 14-12-31.
 */
public abstract class Entity<M> {
  /**
   * Attributes of this model
   */
  public abstract Map<String, Object> getAttrs();

  public abstract M putAttrs(Map<String, Object> attrs);
}
