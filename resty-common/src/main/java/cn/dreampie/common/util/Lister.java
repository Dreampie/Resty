package cn.dreampie.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ice on 14-12-29.
 */
public class Lister {
  public static <T> List<T> of(Object... objects) {
    if (objects == null || objects.length == 0) return new ArrayList<T>();
    return (List<T>) Arrays.asList(objects);
  }
}
