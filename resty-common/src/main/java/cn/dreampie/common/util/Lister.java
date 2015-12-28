package cn.dreampie.common.util;

import cn.dreampie.common.util.serialize.Serializer;

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

  public static <T> List<T> copyOf(List<T> objects) {
    if (objects == null || objects.size() == 0) return new ArrayList<T>();
    return (List<T>) Serializer.unserialize(Serializer.serialize(objects));
  }

  public static <T> List<T> copyOf(List<T> dist, List<T> source) {
    if (source == null || source.size() == 0) return dist;
    dist.addAll((List<T>) Serializer.unserialize(Serializer.serialize(source)));
    return dist;
  }
}
