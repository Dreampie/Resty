package cn.dreampie.orm.meta;

import cn.dreampie.orm.repository.CascadeType;
import cn.dreampie.orm.repository.JoinType;

import java.lang.reflect.Field;

public class SubMeta extends FieldMeta {

  private final Class<?> subClass;
  private final boolean lazy;
  private final String foreign;
  private final CascadeType cascade;
  private final JoinType join;

  public SubMeta(String column, Field field, Class<?> subClass, boolean lazy, String foreign, CascadeType cascade, JoinType join) {
    super(column, field);
    this.subClass = subClass;
    this.lazy = lazy;
    this.foreign = foreign;
    this.cascade = cascade;
    this.join = join;
  }

  public Class<?> getSubClass() {
    return subClass;
  }

  public boolean isLazy() {
    return lazy;
  }

  public String getForeign() {
    return foreign;
  }

  public CascadeType getCascade() {
    return cascade;
  }

  public JoinType getJoin() {
    return join;
  }
}
