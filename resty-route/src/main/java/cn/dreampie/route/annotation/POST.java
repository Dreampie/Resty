package cn.dreampie.route.annotation;

import cn.dreampie.route.valid.Validator;

import java.lang.annotation.*;

/**
 * Annotation used to mark a resource method that responds to HTTP POST requests.
 * 向服务器提交数据。这个方法用途广泛，几乎目前所有的提交操作都是靠这个完成。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface POST {
  String value() default "";

  String[] headers() default {};

  String des() default "";

  Class<? extends Validator>[] valid() default {};
}
