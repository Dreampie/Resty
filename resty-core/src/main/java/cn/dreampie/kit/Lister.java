package cn.dreampie.kit;

import java.util.Arrays;
import java.util.List;

/**
 * Created by ice on 14-12-29.
 */
public class Lister {
  public static <T> List<T> of(Object... objects) {
    return (List<T>) Arrays.asList(objects);
  }
}
