package cn.dreampie.orm.generate;

/**
 * @author Dreampie
 * @date 2015-04-30
 * @what 生成主键
 */
public interface Generator {
  /**
   * 非自动生成主键的产生策略
   *
   * @return object
   */
  public Object generateKey();
}
