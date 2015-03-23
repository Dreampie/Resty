package cn.dreampie.quartz.exception;

/**
 * DBException
 */
public class QuartzException extends RuntimeException {

  public QuartzException() {
  }

  public QuartzException(String message) {
    super(message);
  }

  public QuartzException(Throwable cause) {
    super(cause);
  }

  public QuartzException(String message, Throwable cause) {
    super(message, cause);
  }
}










