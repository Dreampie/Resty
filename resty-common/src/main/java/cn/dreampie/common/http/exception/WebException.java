package cn.dreampie.common.http.exception;

import cn.dreampie.common.http.result.HttpStatus;

/**
 * Created by ice on 14-12-19.
 * A WebException can be raised to make resty return immediately an HTTP response with a specific HTTP status.
 */
public class WebException extends RuntimeException {

  private final HttpStatus status;
  private final Object content;

  public WebException(HttpStatus status) {
    this(status, status.getDesc());
  }

  public WebException(String message) {
    this(HttpStatus.BAD_REQUEST, message);
  }

  public WebException(Object content) {
    this(HttpStatus.BAD_REQUEST, content);
  }

  public WebException(HttpStatus status, String message) {
    super(message);
    this.status = status;
    this.content = null;
  }

  public WebException(HttpStatus status, Object content) {
    super();
    this.status = status;
    this.content = content;
  }

  public HttpStatus getStatus() {
    return status;
  }

  /**
   * Returns the content to use in the HTTP response generated for this exception.
   * <p/>
   * Developer's note: override to provide a content different from the exception message.
   * Alternatively you can override the writeTo method for full control over the response.
   *
   * @return the content to use in the response.
   */
  public Object getContent() {
    return content != null ? content : getMessage();
  }

}
