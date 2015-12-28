package cn.dreampie.orm.generate;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dreampie on 15/12/28.
 */
public class GeneratorFactory {

  public final static String UUID = "uuid";

  private static Map<String, Generator> generatorMap = new HashMap<String, Generator>() {{
    put(UUID, new UUIDGenerator());
  }};

  public static void add(String type, Generator generator) {
    if (!(generator instanceof UUIDGenerator)) {
      generatorMap.put(type, generator);
    }
  }

  /**
   * 返回新的generator对象
   *
   * @param type 扩展名
   * @return
   */
  public static Generator get(String type) {
    Generator generator = generatorMap.get(type);
    if (generator == null && !type.isEmpty()) {
      throw new IllegalArgumentException("Could not found generator for type: " + type);
    } else {
      return generator;
    }
  }

  public static boolean contains(String type) {
    return generatorMap.containsKey(type);
  }

}
