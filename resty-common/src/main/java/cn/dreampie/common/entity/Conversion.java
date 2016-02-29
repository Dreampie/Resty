package cn.dreampie.common.entity;

/**
 * Created by Dreampie on 16/2/29.
 */
public interface Conversion {

  public Object read(Object v);

  public Object write(Object v);
}
