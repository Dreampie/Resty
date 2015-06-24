package cn.dreampie.common.util.scan;

import java.lang.annotation.Annotation;

/**
 * Created by ice on 14-12-19.
 */
public class AnnotationScaner extends Scaner {

  private Class<? extends Annotation>[] targets;
  private boolean or = true;

  public AnnotationScaner(Class<? extends Annotation>... targets) {
    this.targets = targets;
  }

  public AnnotationScaner(boolean or, Class<? extends Annotation>... targets) {
    this.or = or;
    this.targets = targets;
  }

  public static AnnotationScaner of(Class<? extends Annotation> target) {
    return new AnnotationScaner(target);
  }

  /**
   * 要扫描的类父级
   *
   * @param targets class
   * @return scaner
   */
  public static AnnotationScaner or(Class<? extends Annotation>... targets) {
    return new AnnotationScaner(targets);
  }

  public static AnnotationScaner and(Class<? extends Annotation>... targets) {
    return new AnnotationScaner(false, targets);
  }

  /**
   * 检测目标类
   *
   * @param clazz
   * @return
   */
  public boolean checkTarget(Class<?> clazz) {
    boolean result = !or;
    for (Class<? extends Annotation> target : targets) {
      if (or) {
        result = clazz.getAnnotation(target) != null;
        if (result) break;
      } else {
        result = result && clazz.getAnnotation(target) != null;
        if (!result) break;
      }
    }
    return result;
  }
}