package cn.dreampie.common.annotation;

import java.lang.annotation.*;

/**
 * Annotation used to mark a resource class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Resource {

  String name() default "";//ioc 注入名字

  boolean singleton() default true;//是否使用单例

  String value();
}
