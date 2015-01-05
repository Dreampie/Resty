package cn.dreampie.log;

import cn.dreampie.log.provider.JdkLoggerProvider;
import cn.dreampie.log.provider.LoggerProvider;
import cn.dreampie.log.provider.Slf4jLoggerProvider;

/**
 * Created by ice on 14-12-19.
 */
public abstract class Logger {

  private static LoggerProvider loggerProvider;

  static {
    try {
      Class.forName("org.slf4j.Logger");
      loggerProvider = new Slf4jLoggerProvider();
    } catch (ClassNotFoundException ex) {
      loggerProvider = new JdkLoggerProvider();
    }
  }

  public static Logger getLogger(Class clazz) {
    return loggerProvider.getLogger(clazz);
  }

  public static Logger getLogger(String clazzName) {
    return loggerProvider.getLogger(clazzName);
  }


  public abstract void debug(String message);

  public abstract void debug(String format, Object... args);

  public abstract void debug(String message, Throwable t);

  public abstract void debug(String format, Throwable t, Object... args);

  public abstract void info(String message);

  public abstract void info(String format, Object... args);

  public abstract void info(String message, Throwable t);

  public abstract void info(String format, Throwable t, Object... args);

  public abstract void warn(String message);

  public abstract void warn(String format, Object... args);

  public abstract void warn(String message, Throwable t);

  public abstract void warn(String format, Throwable t, Object... args);

  public abstract void error(String message);

  public abstract void error(String format, Object... args);

  public abstract void error(String message, Throwable t);

  public abstract void error(String format, Throwable t, Object... args);

  public abstract boolean isDebugEnabled();

  public abstract boolean isInfoEnabled();

  public abstract boolean isWarnEnabled();

  public abstract boolean isErrorEnabled();
}
