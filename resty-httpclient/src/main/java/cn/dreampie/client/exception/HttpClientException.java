package cn.dreampie.client.exception;

/**
 * HttpClientException
 */
public class HttpClientException extends RuntimeException {

  /**
   * Constructor.
   */
  public HttpClientException() {
  }

  /**
   * Constructor.
   *
   * @param message Exception message.
   */
  public HttpClientException(String message) {
    super(message);
  }

  /**
   * Constructor.
   *
   * @param cause An exception.
   */
  public HttpClientException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructor.
   *
   * @param message Exception message.
   * @param cause   An exception.
   */
  public HttpClientException(String message, Throwable cause) {
    super(message, cause);
  }

}










