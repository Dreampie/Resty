package cn.dreampie.common.util.properties;


import java.io.*;
import java.util.Properties;

/**
 * Prop. Prop can load properties file from CLASSPATH or File object.
 */
public class Prop {

  public static final String DEFAULT_ENCODING = "UTF-8";

  private Properties properties = null;

  /**
   * Prop constructor.
   *
   * @see #Prop(String, String)
   */
  public Prop(String fileName) {
    this(fileName, DEFAULT_ENCODING);
  }

  /**
   * Prop constructor
   * <p/>
   * Example:<br>
   * Prop prop = new Prop("my_config.txt", "UTF-8");<br>
   * String userName = prop.get("userName");<br><br>
   * <p/>
   * prop = new Prop("com/jfinal/file_in_sub_path_of_classpath.txt", "UTF-8");<br>
   * String value = prop.get("key");
   *
   * @param fileName the properties file's name in classpath or the sub directory of classpath
   * @param encoding the encoding
   */
  public Prop(String fileName, String encoding) {
    InputStream inputStream = null;
    try {
      inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);    // properties.load(Prop.class.getResourceAsStream(fileName));
      if (inputStream == null)
        throw new IllegalArgumentException("Properties file not found in classpath: " + fileName);
      properties = new Properties();
      properties.load(new InputStreamReader(inputStream, encoding));
    } catch (IOException e) {
      throw new RuntimeException("Error loading properties file.", e);
    } finally {
      if (inputStream != null) try {
        inputStream.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Prop constructor.
   *
   * @see #Prop(java.io.File, String)
   */
  public Prop(File file) {
    this(file, DEFAULT_ENCODING);
  }

  /**
   * Prop constructor
   * <p/>
   * Example:<br>
   * Prop prop = new Prop(new File("/var/config/my_config.txt"), "UTF-8");<br>
   * String userName = prop.get("userName");
   *
   * @param file     the properties File object
   * @param encoding the encoding
   */
  public Prop(File file, String encoding) {
    if (file == null)
      throw new IllegalArgumentException("File can not be null.");
    if (file.isFile() == false)
      throw new IllegalArgumentException("Not a file : " + file.getName());

    InputStream inputStream = null;
    try {
      inputStream = new FileInputStream(file);
      properties = new Properties();
      properties.load(new InputStreamReader(inputStream, encoding));
    } catch (IOException e) {
      throw new RuntimeException("Error loading properties file.", e);
    } finally {
      if (inputStream != null) try {
        inputStream.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
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
