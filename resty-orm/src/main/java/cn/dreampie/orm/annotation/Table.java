package cn.dreampie.orm.annotation;

import cn.dreampie.orm.Base;
import cn.dreampie.orm.generate.DefaultGenerator;
import cn.dreampie.orm.generate.Generator;

import java.lang.annotation.*;

/**
 * Created by ice on 14-12-30.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface Table {
  String name();//表名

  String generatedKey() default Base.DEFAULT_GENERATED_KEY;//自动生成的主键

  Class<? extends Generator> generator() default DefaultGenerator.class;//主键生成策略

  boolean generated() default false;//使用自定义的主键策略生成主键

  String[] primaryKey() default {};//非自动生成的主键放在这儿

  boolean cached() default false;//是否使用缓存

  int expired() default -1;//缓存过期时间 默认在更新时过期，或者在缓存配置文件中设置过期时间
}
