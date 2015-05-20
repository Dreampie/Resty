package cn.dreampie.orm.generate;

import java.io.Serializable;

/**
 * @author Dreampie
 * @date 2015-04-30
 * @what 生成主键
 */
public interface Generator extends Serializable {
  /**
   * 非自动生成主键的产生策略
   *
   * @return object
   */
  public Object generateKey();
}
