package cn.dreampie.common.entity.exception;

/**
 * ModelException
 */
public class EntityException extends RuntimeException {

  public EntityException() {
  }

  public EntityException(String message) {
    super(message);
  }

  public EntityException(Throwable cause) {
    super(cause);
  }

  public EntityException(String message, Throwable cause) {
    super(message, cause);
  }
}










