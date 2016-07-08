package cn.dreampie.client.exception;

/**
 * ClientException
 */
public class ClientException extends RuntimeException {

  /**
   * Constructor.
   */
  public ClientException() {
  }

  /**
   * Constructor.
   *
   * @param message Exception message.
   */
  public ClientException(String message) {
    super(message);
  }

  /**
   * Constructor.
   *
   * @param cause An exception.
   */
  public ClientException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructor.
   *
   * @param message Exception message.
   * @param cause   An exception.
   */
  public ClientException(String message, Throwable cause) {
    super(message, cause);
  }

}










