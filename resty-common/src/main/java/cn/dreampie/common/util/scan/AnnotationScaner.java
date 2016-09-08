package cn.dreampie.common.util.scan;

import java.lang.annotation.Annotation;

/**
 * Created by ice on 14-12-19.
 */
public class AnnotationScaner extends Scaner<AnnotationScaner> {

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
    return new AnnotationScaner(target).scanInJar(true).targetPattern("*.class");
  }

  /**
   * 检测目标类
   *
   * @param clazz
   * @return
   */
  public boolean checkTarget(Object clazz) {
    return ((Class<?>) clazz).getAnnotation(target) != null;
  }

  public String packageFilePathSolve(String filePath) {
    filePath = filePath.substring(filePath.indexOf("classes/") + "classes/".length(), filePath.indexOf(".class"));
    return filePath.replaceAll("/", ".");
  }

  public String jarFilePathSolve(String filePath) {
    return filePath.replaceAll("/", ".").substring(0, filePath.length() - 6);
  }
}