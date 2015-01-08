package cn.dreampie.common.util;

import java.io.*;
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

  public static <T> List<T> copyOf(List<T> objects) throws IOException, ClassNotFoundException {
    if (objects == null || objects.size() == 0) return new ArrayList<T>();
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    ObjectOutputStream out = new ObjectOutputStream(byteOut);
    out.writeObject(objects);

    ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
    ObjectInputStream in = new ObjectInputStream(byteIn);
    return (List<T>) in.readObject();
  }
}
