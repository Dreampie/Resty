package cn.dreampie.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by ice on 14-12-30.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Table {
  String name();

  String primaryKey() default "id";//多主键策略 1自增主键+n其他主键  自增主键放在第一位

  boolean lockKey() default false;//锁定主键策略，当1自增主键+n其他主键时，锁定主键表示增删改查都必须检测主键的完整性

  boolean cached() default false;
}
