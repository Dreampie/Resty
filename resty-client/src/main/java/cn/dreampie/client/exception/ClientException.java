package cn.dreampie.client.exception;

import cn.dreampie.common.http.result.HttpStatus;

/**
 * ClientException
 */
public class ClientException extends RuntimeException {
  private HttpStatus status;

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
  public ClientException(HttpStatus status) {
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
  public ClientException(String message, Throwable cause, HttpStatus status) {
    super(message, cause);
    this.status = status;
  }

  /**
   * Constructor.
   *
   * @param message Exception message.
   * @param status  It's usually a HTTP Status Code (404, 500, etc.)
   */
  public ClientException(String message, HttpStatus status) {
    super(message);
    this.status = status;
  }

  /**
   * Constructor.
   *
   * @param cause  An exception.
   * @param status It's usually a HTTP Status Code (404, 500, etc.)
   */
  public ClientException(Throwable cause, HttpStatus status) {
    super(cause);
    this.status = status;
  }

  /**
   * Get the exception's status. It's usually a HTTP Status Code (404, 500, etc.)
   */
  public HttpStatus getStatus() {
    return this.status;
  }
}










