package cn.dreampie.orm.activerecord.exception;

/**
 * DBException
 */
public class ActiveRecordException extends RuntimeException {

  public ActiveRecordException() {
  }

  public ActiveRecordException(String message) {
    super(message);
  }

  public ActiveRecordException(Throwable cause) {
    super(cause);
  }

  public ActiveRecordException(String message, Throwable cause) {
    super(message, cause);
  }
}










