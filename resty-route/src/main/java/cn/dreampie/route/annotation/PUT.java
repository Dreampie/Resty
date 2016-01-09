package cn.dreampie.route.annotation;

import cn.dreampie.route.valid.Validator;

import java.lang.annotation.*;

/**
 * Annotation used to mark a resource method that responds to HTTP PUT requests.
 * 这个方法比较少见。HTML表单也不支持这个。本质上来讲， PUT和POST极为相似，都是向服务器发送数据，但它们之间有一个重要区别，PUT通常指定了资源的存放位置，而POST则没有，POST的数据存放位置由服务器自己决定。
 * 举个例子：如一个用于提交博文的URL，/addBlog。如果用PUT，则提交的URL会是像这样的”/addBlog/abc123”，其中abc123就是这个博文的地址。而如果用POST，则这个地址会在提交后由服务器告知客户端。目前大部分博客都是这样的。
 * 显然，PUT和POST用途是不一样的。具体用哪个还取决于当前的业务场景。
 * PUT 用于更新某个资源较完整的内容，比如说用户要重填完整表单更新所有信息，后台处理更新时可能只是保留内部记录 ID 不
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface PUT {
  String value() default "";

  String[] headers() default {};

  String des() default "";

  Class<? extends Validator>[] valid() default {};
}
