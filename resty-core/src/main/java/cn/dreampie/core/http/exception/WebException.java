package cn.dreampie.core.http.exception;

import cn.dreampie.core.base.Request;
import cn.dreampie.core.base.Response;
import cn.dreampie.core.http.HttpStatus;
import cn.dreampie.log.Logger;
import cn.dreampie.log.LoggerFactory;

import java.io.IOException;

/**
 * Created by ice on 14-12-19.
 * A WebException can be raised to make resty return immediately an HTTP response with a specific HTTP status.
 */
public class WebException extends RuntimeException {
  private final static Logger LOGGER = LoggerFactory.getLogger(WebException.class);

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

  public WebException(Exception e) {
    super(e);
    this.status = HttpStatus.BAD_REQUEST;
  }

  public WebException(HttpStatus status, Exception e) {
    super(e);
    this.status = status;
  }

  public HttpStatus getStatus() {
    return status;
  }

  /**
   * Returns the content type to use in the HTTP response generated for this exception.
   * <p/>
   * Developer's note: override to provide another content type than text-plain
   * Alternatively you can override the writeTo method for full control over the response.
   *
   * @return the content type to use in the response.
   */
  public String getContentType() {
    return "text/plain";
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

  /**
   * Writes this web exception in a RestyResponse.
   * <p/>
   * This implementation uses the status, contentType and content found on the exception.
   * Override it to provide custom response.
   * Note that the implementation is also responsible for logging the exception.
   *
   * @param request
   * @param response the response to write to
   * @throws java.io.IOException in case of IO error.
   */
  public void writeTo(Request request, Response response) throws IOException {
    // by default log stack trace at debug level only
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("request raised WebException - " + request, this);
    }

    response.setStatus(getStatus());
    response.setContentType(getContentType());
    response.getWriter().print(getContent());
  }
}
