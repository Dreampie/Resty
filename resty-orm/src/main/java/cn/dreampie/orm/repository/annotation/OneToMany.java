package cn.dreampie.orm.repository.annotation;

import cn.dreampie.orm.repository.CascadeType;
import cn.dreampie.orm.repository.JoinType;

import java.lang.annotation.*;

/**
 * Created by ice on 14-12-30.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface OneToMany {
  String name();//列名

  String foreign() default "";//外键列

  boolean lazy() default true;//延迟加载

  CascadeType cascade() default CascadeType.QUERY;//级联方式

  JoinType join() default JoinType.JOIN;//join方式
}

