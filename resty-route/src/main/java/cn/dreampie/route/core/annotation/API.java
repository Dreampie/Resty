package cn.dreampie.route.core.annotation;

import java.lang.annotation.*;

/**
 * Annotation used to mark a resource class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface API {
  String value();
}
