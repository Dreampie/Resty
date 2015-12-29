package cn.dreampie.orm.annotation;

import cn.dreampie.orm.Base;

import java.lang.annotation.*;

/**
 * Created by ice on 14-12-30.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface Table {
  String name();//表名

  String generatedKey() default Base.DEFAULT_GENERATED_KEY;//自动生成的主键 如果没有自动生成的主键设置为空字符串

  String sequence() default "";//序列值

  String generatedType() default "";//自定义主键生成策略 GeneratorFactory.UUID

  String[] primaryKey() default {};//非自动生成的主键放在这儿

  boolean cached() default false;//是否使用缓存

  int expired() default -1;//缓存过期时间 默认在更新时过期，或者在缓存配置文件中设置过期时间
}
