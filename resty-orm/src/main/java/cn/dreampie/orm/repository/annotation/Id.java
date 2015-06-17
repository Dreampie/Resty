package cn.dreampie.orm.repository.annotation;

import cn.dreampie.common.generate.DefaultGenerator;
import cn.dreampie.common.generate.Generator;
import cn.dreampie.orm.repository.GenerateType;

import java.lang.annotation.*;

/**
 * Created by ice on 14-12-30.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface Id {
  String name();//列名

  boolean nullable() default true;//是否可以为空

  GenerateType generate() default GenerateType.AUTO;

  Class<? extends Generator> generator() default DefaultGenerator.class;//id生成策略
}

