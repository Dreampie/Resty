package cn.dreampie.log;


import java.util.logging.Level;

/**
 * Created by ice on 14-12-19.
 */
public class JdkLogger implements Logger {

  private java.util.logging.Logger logger;
  private String clazzName;

  JdkLogger() {
    super();
  }

  public Logger getLogger(Class clazz) {
    this.clazzName = clazz.getName();
    logger = java.util.logging.Logger.getLogger(clazz.getName());
    return this;
  }

  public Logger getLogger(String clazzName) {
    this.clazzName = clazzName;
    logger = java.util.logging.Logger.getLogger(clazzName);
    return this;
  }

  public void debug(String message) {
    logger.logp(Level.FINE, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message);
  }

  public void debug(String message, Throwable t) {
    logger.logp(Level.FINE, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message, t);
  }

  public void debug(String message, Object... args) {
    logger.logp(Level.FINE, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), String.format(message, args));
  }

  public void debug(String message, Throwable t, Object... args) {
    logger.logp(Level.FINE, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), String.format(message, args), t);
  }


  public void info(String message) {
    logger.logp(Level.INFO, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message);
  }

  public void info(String message, Throwable t) {
    logger.logp(Level.INFO, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message, t);
  }

  public void info(String message, Object... args) {
    logger.logp(Level.INFO, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), String.format(message, args));
  }

  public void info(String message, Throwable t, Object... args) {
    logger.logp(Level.INFO, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), String.format(message, args), t);
  }

  public void warn(String message) {
    logger.logp(Level.WARNING, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message);
  }

  public void warn(String message, Throwable t) {
    logger.logp(Level.WARNING, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message, t);
  }

  public void warn(String message, Object... args) {
    logger.logp(Level.WARNING, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), String.format(message, args));
  }

  public void warn(String message, Throwable t, Object... args) {
    logger.logp(Level.WARNING, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), String.format(message, args), t);
  }

  public void error(String message) {
    logger.logp(Level.SEVERE, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message);
  }

  public void error(String message, Throwable t) {
    logger.logp(Level.SEVERE, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), message, t);
  }

  public void error(String message, Object... args) {
    logger.logp(Level.SEVERE, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), String.format(message, args));
  }

  public void error(String message, Throwable t, Object... args) {
    logger.logp(Level.SEVERE, clazzName, Thread.currentThread().getStackTrace()[1].getMethodName(), String.format(message, args), t);
  }

  public boolean isDebugEnabled() {
    return logger.isLoggable(Level.FINE);
  }


  public boolean isInfoEnabled() {
    return logger.isLoggable(Level.INFO);
  }


  public boolean isWarnEnabled() {
    return logger.isLoggable(Level.WARNING);
  }


  public boolean isErrorEnabled() {
    return logger.isLoggable(Level.SEVERE);
  }

}
