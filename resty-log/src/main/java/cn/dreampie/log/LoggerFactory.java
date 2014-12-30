package cn.dreampie.log;

/**
 * Created by ice on 14-12-19.
 */
public class LoggerFactory {

  private static LoggerFactory loggerFactory = new LoggerFactory();
  private Logger logger;

  LoggerFactory() {
    try {
      Class.forName("org.slf4j.Logger");
      logger = new Slf4JLogger();
    } catch (ClassNotFoundException ex) {
      logger = new JdkLogger();
    }
  }

  public static Logger getLogger(Class clazz) {
    return loggerFactory.logger.getLogger(clazz);
  }

  public static Logger getLogger(String clazzName) {
    return loggerFactory.logger.getLogger(clazzName);
  }


}
