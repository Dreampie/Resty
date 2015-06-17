package cn.dreampie.orm.repository.annotation;

import cn.dreampie.common.CaseStyle;

import java.lang.annotation.*;

/**
 * Created by ice on 14-12-30.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface Entity {
  String table();//表名

  CaseStyle style() default CaseStyle.UNDERSCORE;//属性风格

  boolean cached() default false;//是否使用缓存
}
