package cn.dreampie.route.annotation;

import cn.dreampie.route.valid.Validator;

import java.lang.annotation.*;

/**
 * Annotation used to mark a resource method that responds to HTTP GET requests.
 * GET可以说是最常见的了，它本质就是发送一个请求来取得服务器上的某一资源。资源通过一组HTTP头和呈现数据（如HTML文本，或者图片或者视频等）返回给客户端。GET请求中，永远不会包含呈现数据。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface GET {
  String value() default "";

  String[] headers() default {};

  String des() default "";

  Class<? extends Validator>[] valid() default {};

  boolean cached() default true;
}
