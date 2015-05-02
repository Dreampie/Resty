package cn.dreampie.common.util.scan;

import java.lang.annotation.Annotation;

/**
 * Created by ice on 14-12-19.
 */
public class AnnotationScaner extends Scaner {

  private Class<? extends Annotation> target;

  public AnnotationScaner(Class<? extends Annotation> target) {
    this.target = target;
  }

  /**
   * 要扫描的类父级
   *
   * @param target class
   * @return scaner
   */
  public static AnnotationScaner of(Class<? extends Annotation> target) {
    return new AnnotationScaner(target);
  }

  /**
   * 检测目标类
   *
   * @param clazz
   * @return
   */
  public boolean checkTarget(Class<?> clazz) {
    return clazz.getAnnotation(target) != null;
  }
}