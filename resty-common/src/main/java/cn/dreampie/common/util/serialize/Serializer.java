package cn.dreampie.common.util.serialize;

import java.io.*;

/**
 * Created by Dreampie on 15/4/24.
 */
public class Serializer {
  /**
   * 序列化
   *
   * @param object 对象
   * @return byte[]
   */
  public static byte[] serialize(Object object) {
    ObjectOutputStream oos = null;
    ByteArrayOutputStream baos = null;
    try {
      if (object != null) {
        baos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(baos);
        oos.writeObject(object);
        return baos.toByteArray();
      } else {
        return null;
      }
    } catch (IOException e) {
      throw new SerializeException(e.getMessage(), e);
    }
  }

  /**
   * 反序列化
   *
   * @param bytes byte数据
   * @return Object
   */
  public static Object unserialize(byte[] bytes) {
    ByteArrayInputStream bais = null;
    try {
      if (bytes != null) {
        bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        return ois.readObject();
      } else {
        return null;
      }
    } catch (Exception e) {
      throw new SerializeException(e.getMessage(), e);
    }
  }

}
