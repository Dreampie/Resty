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
public @interface Column {
  String name();//列名
}

