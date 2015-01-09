package cn.dreampie.route.core.annotation;

import java.lang.annotation.*;

/**
 * Annotation used to mark a resource class.
 * 可以被继承
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface API {
  String value();
}
