package cn.dreampie.log.provider;

import cn.dreampie.log.Logger;

/**
 * Created by ice on 15-1-5.
 */
public class Slf4jLoggerProvider implements LoggerProvider {

  public Logger getLogger(Class clazz) {
    return new Slf4JLogger(org.slf4j.LoggerFactory.getLogger(clazz));
  }

  public Logger getLogger(String clazzName) {
    return new Slf4JLogger(org.slf4j.LoggerFactory.getLogger(clazzName));
  }


  public class Slf4JLogger extends Logger {

    private org.slf4j.Logger logger;

    Slf4JLogger(org.slf4j.Logger logger) {
      this.logger = logger;
    }

    public void debug(String message) {
      logger.debug(message);
    }

    public void debug(String message, Object... args) {
      logger.debug(String.format(message, args));
    }

    public void debug(String message, Throwable t) {
      logger.debug(message, t);
    }

    public void debug(String message, Throwable t, Object... args) {
      logger.debug(String.format(message, args), t);
    }

    public void info(String message) {
      logger.info(message);
    }

    public void info(String message, Object... args) {
      logger.info(String.format(message, args));
    }

    public void info(String message, Throwable t) {
      logger.info(message, t);
    }

    public void info(String message, Throwable t, Object... args) {
      logger.info(String.format(message, args), t);
    }

    public void warn(String message) {
      logger.warn(message);
    }

    public void warn(String message, Object... args) {
      logger.warn(String.format(message, args));
    }

    public void warn(String message, Throwable t) {
      logger.warn(message, t);
    }

    public void warn(String message, Throwable t, Object... args) {
      logger.warn(String.format(message, args), t);
    }

    public void error(String message) {
      logger.error(message);
    }

    public void error(String message, Object... args) {
      logger.error(String.format(message, args));
    }

    public void error(String message, Throwable t) {
      logger.error(message, t);
    }

    public void error(String message, Throwable t, Object... args) {
      logger.error(String.format(message, args), t);
    }

    public boolean isDebugEnabled() {
      return logger.isDebugEnabled();
    }


    public boolean isInfoEnabled() {
      return logger.isInfoEnabled();
    }


    public boolean isWarnEnabled() {
      return logger.isWarnEnabled();
    }


    public boolean isErrorEnabled() {
      return logger.isErrorEnabled();
    }
  }
}
