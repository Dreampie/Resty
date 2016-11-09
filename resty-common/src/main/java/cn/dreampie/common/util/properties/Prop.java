package cn.dreampie.common.util.properties;


import cn.dreampie.common.Constant;
import cn.dreampie.log.Logger;

import java.io.*;
import java.util.Properties;
import java.util.Set;

/**
 * Prop. Prop can load properties file from CLASSPATH or File object.
 */
public class Prop {
  private final static Logger logger = Logger.getLogger(Prop.class);

  private Properties properties = null;

  /**
   * Prop constructor.
   *
   * @see #Prop(String, String)
   */
  public Prop(String fileName) {
    this(fileName, Constant.encoding);
  }

  /**
   * Prop constructor
   * <p/>
   * Example:<br>
   * Prop prop = new Prop("my_config.txt", "UTF-8");<br>
   * String userName = prop.getMessage("userName");<br><br>
   * <p/>
   * prop = new Prop("com/resty/file_in_sub_path_of_classpath.txt", "UTF-8");<br>
   * String value = prop.getMessage("key");
   *
   * @param fileName the properties file's name in classpath or the sub directory of classpath
   * @param encoding the encoding
   */
  public Prop(String fileName, String encoding) {
    InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
    load(fileName, inputStream, encoding);
  }

  /**
   * Prop constructor.
   *
   * @see #Prop(java.io.File, String)
   */
  public Prop(File file) {
    this(file, Constant.encoding);
  }

  /**
   * Prop constructor
   * <p/>
   * Example:<br>
   * Prop prop = new Prop(new File("/var/config/my_config.txt"), "UTF-8");<br>
   * String userName = prop.getMessage("userName");
   *
   * @param file     the properties File object
   * @param encoding the encoding
   */
  public Prop(File file, String encoding) {
    if (file == null)
      throw new IllegalArgumentException("File can not be null.");
    String fileName = file.getName();
    if (!file.isFile())
      throw new IllegalArgumentException("Not a file : " + fileName);
    InputStream inputStream;
    try {
      inputStream = new FileInputStream(file);
      load(fileName, inputStream, encoding);
    } catch (FileNotFoundException e) {
      logger.warn(e.getMessage(), e);
    }
  }

  void load(String fileName, InputStream inputStream, String encoding) {
    if (inputStream == null)
      throw new IllegalArgumentException("Properties file not found in classpath: " + fileName);
    try {
      properties = new Properties();
      properties.load(new InputStreamReader(inputStream, encoding == null ? "UTF-8" : encoding));
    } catch (IOException e) {
      throw new RuntimeException("Error loading properties file.", e);
    } finally {
      try {
        inputStream.close();
      } catch (IOException e) {
        logger.warn(e.getMessage(), e);
      }
    }
  }

  public Set<String> getKeys() {
    return properties.stringPropertyNames();
  }

  public String get(String key) {
    return properties.getProperty(key);
  }

  public String get(String key, String defaultValue) {
    String value = get(key);
    return (value != null) ? value : defaultValue;
  }

  public Integer getInt(String key) {
    String value = get(key);
    return (value != null) ? Integer.parseInt(value) : null;
  }

  public Integer getInt(String key, Integer defaultValue) {
    String value = get(key);
    return (value != null) ? Integer.parseInt(value) : defaultValue;
  }

  public Long getLong(String key) {
    String value = get(key);
    return (value != null) ? Long.parseLong(value) : null;
  }

  public Long getLong(String key, Long defaultValue) {
    String value = get(key);
    return (value != null) ? Long.parseLong(value) : defaultValue;
  }

  public Boolean getBoolean(String key) {
    String value = get(key);
    return (value != null) ? Boolean.parseBoolean(value) : null;
  }

  public Boolean getBoolean(String key, Boolean defaultValue) {
    String value = get(key);
    return (value != null) ? Boolean.parseBoolean(value) : defaultValue;
  }

  public boolean containsKey(String key) {
    return properties.containsKey(key);
  }

  public Properties getProperties() {
    return properties;
  }
}
