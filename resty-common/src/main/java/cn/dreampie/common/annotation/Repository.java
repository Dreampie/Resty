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
//
//  String tableName();//表名
//
//  String generatedKey() default Entity.DEFAULT_GENERATED_KEY;//自动生成的主键
//
//  Class<? extends Generator> generator() default DefaultGenerator.class;//主键生成策略
//
//  boolean generated() default false;//使用自定义的主键策略生成主键
//
//  String[] primaryKey() default {};//非自动生成的主键放在这儿
//
//  boolean cached() default false;//是否使用缓存
}
