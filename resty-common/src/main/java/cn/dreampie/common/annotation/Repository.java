package cn.dreampie.common.annotation;

import java.lang.annotation.*;

/**
 * Created by ice on 14-12-30.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface Repository {
  String name() default "";

  boolean singleton() default true;//是否使用单例

}
