package cn.dreampie.common.util.scan;

/**
 * Created by ice on 14-12-19.
 */
public class ClassScaner extends Scaner {

  private Class<?>[] targets;
  private boolean or = true;

  public ClassScaner(Class<?>... targets) {
    this.targets = targets;
  }

  public ClassScaner(boolean or, Class<?>... targets) {
    this.or = or;
    this.targets = targets;
  }

  public static ClassScaner of(Class<?> target) {
    return new ClassScaner(target);
  }

  /**
   * 要扫描的类父级
   *
   * @param targets class
   * @return scaner
   */
  public static ClassScaner or(Class<?>... targets) {
    return new ClassScaner(targets);
  }

  public static ClassScaner and(Class<?>... targets) {
    return new ClassScaner(false, targets);
  }

  /**
   * 检测目标类
   *
   * @param clazz
   * @return
   */
  public boolean checkTarget(Class<?> clazz) {
    boolean result = !or;
    for (Class<?> target : targets) {
      if (or) {
        result = target.isAssignableFrom(clazz) && target != clazz;
        if (result) break;
      } else {
        result = result && target.isAssignableFrom(clazz) && target != clazz;
        if (!result) break;
      }
    }
    return result;
  }
}