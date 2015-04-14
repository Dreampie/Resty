package cn.dreampie.common.http.result;

/**
 * Created by ice on 14-12-19.
 * A WebResult can be raised to make resty return immediately an HTTP response with a specific HTTP status.
 */
public class WebResult {

  private final HttpStatus status;
  private final Object result;

  public WebResult(HttpStatus status) {
    this.status = status;
    this.result = null;
  }

  public WebResult(HttpStatus status, Object result) {
    this.status = status;
    this.result = result;
  }

  public WebResult(Object result) {
    this.status = HttpStatus.OK;
    this.result = result;
  }

  public HttpStatus getStatus() {
    return status;
  }

  /**
   * Returns the content to use in the HTTP response .
   *
   * @return the content to use in the response.
   */
  public Object getResult() {
    return result;
  }

}
