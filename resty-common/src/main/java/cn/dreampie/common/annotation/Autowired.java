package cn.dreampie.common.annotation;

import java.lang.annotation.*;

/**
 * Annotation used to mark a resource class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Autowired {

  String name() default "";//ioc 注入名字

  boolean required() default true;//是否强制检查注入对象是存在
}
