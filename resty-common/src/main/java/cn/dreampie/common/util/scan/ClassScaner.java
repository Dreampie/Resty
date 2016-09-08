package cn.dreampie.common.util.scan;

/**
 * Created by ice on 14-12-19.
 */
public class ClassScaner extends Scaner<ClassScaner> {

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
    return new ClassScaner(target).scanInJar(true).targetPattern("*.class");
  }

  /**
   * 检测目标类
   *
   * @param clazz
   * @return
   */
  public boolean checkTarget(Object clazz) {
    return this.target.isAssignableFrom((Class<?>) clazz) && this.target != clazz;
  }

  public String packageFilePathSolve(String filePath) {
    filePath = filePath.substring(filePath.indexOf("classes/") + "classes/".length(), filePath.indexOf(".class"));
    return filePath.replaceAll("/", ".");
  }

  public String jarFilePathSolve(String filePath) {
    return filePath.replaceAll("/", ".").substring(0, filePath.length() - 6);
  }
}