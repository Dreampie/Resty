package cn.dreampie.route.http.exception;

import cn.dreampie.log.Logger;
import cn.dreampie.route.http.HttpStatus;

/**
 * Created by ice on 14-12-19.
 * A WebException can be raised to make resty return immediately an HTTP response with a specific HTTP status.
 */
public class WebException extends RuntimeException {
  private final static Logger logger = Logger.getLogger(WebException.class);

  private final HttpStatus status;

  public WebException(HttpStatus status) {
    super(status.getDesc());
    this.status = status;
  }

  public WebException(HttpStatus status, String message) {
    super(message);
    this.status = status;
  }

  public WebException(String message) {
    super(message);
    this.status = HttpStatus.BAD_REQUEST;
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
  public String getContent() {
    return getMessage();
  }

}
