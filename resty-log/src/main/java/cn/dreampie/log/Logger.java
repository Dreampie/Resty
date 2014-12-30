package cn.dreampie.log;

/**
 * Created by ice on 14-12-19.
 */
public interface Logger {
  public Logger getLogger(Class clazz);

  public Logger getLogger(String clazzName);

  public void debug(String message);

  public void debug(String format, Object... args);

  public void debug(String message, Throwable t);

  public void debug(String format, Throwable t, Object... args);

  public void info(String message);

  public void info(String format, Object... args);

  public void info(String message, Throwable t);

  public void info(String format, Throwable t, Object... args);

  public void warn(String message);

  public void warn(String format, Object... args);

  public void warn(String message, Throwable t);

  public void warn(String format, Throwable t, Object... args);

  public void error(String message);

  public void error(String format, Object... args);

  public void error(String message, Throwable t);

  public void error(String format, Throwable t, Object... args);

  public boolean isDebugEnabled();

  public boolean isInfoEnabled();

  public boolean isWarnEnabled();

  public boolean isErrorEnabled();
}
