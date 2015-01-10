package cn.dreampie.client.exception;

/**
 * ActiveRecordException
 */
public class ClientException extends RuntimeException {
  private int status;

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


  /**
   * Constructor.
   *
   * @param status It's usually a HTTP Status Code (404, 500, etc.)
   */
  public ClientException(int status) {
    super();
    this.status = status;
  }

  /**
   * Constructor.
   *
   * @param message Exception message.
   * @param cause   An exception.
   * @param status  It's usually a HTTP Status Code (404, 500, etc.)
   */
  public ClientException(String message, Throwable cause, int status) {
    super(message, cause);
    this.status = status;
  }

  /**
   * Constructor.
   *
   * @param message Exception message.
   * @param status  It's usually a HTTP Status Code (404, 500, etc.)
   */
  public ClientException(String message, int status) {
    super(message);
    this.status = status;
  }

  /**
   * Constructor.
   *
   * @param cause  An exception.
   * @param status It's usually a HTTP Status Code (404, 500, etc.)
   */
  public ClientException(Throwable cause, int status) {
    super(cause);
    this.status = status;
  }

  /**
   * Get the exception's status. It's usually a HTTP Status Code (404, 500, etc.)
   */
  public int getStatus() {
    return this.status;
  }
}










