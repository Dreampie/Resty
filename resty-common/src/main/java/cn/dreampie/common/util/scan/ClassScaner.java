package cn.dreampie.common.util.scan;

/**
 * Created by ice on 14-12-19.
 */
public class ClassScaner extends Scaner {

  private Class<?> target;

  public ClassScaner(Class<?> target) {
    this.target = target;
  }

  /**
   * 要扫描的类父级
   *
   * @param target class
   * @return scaner
   */
  public static ClassScaner of(Class<?> target) {
    return new ClassScaner(target);
  }

  /**
   * 检测目标类
   *
   * @param clazz
   * @return
   */
  public boolean checkTarget(Class<?> clazz) {
    return target.isAssignableFrom(clazz) && target != clazz;
  }
}