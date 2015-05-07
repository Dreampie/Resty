package cn.dreampie.common.http.result;

/**
 * Created by ice on 14-12-19.
 * A WebResult can be raised to make resty return immediately an HTTP response with a specific HTTP status.
 */
public class WebResult<T> {

  private final HttpStatus status;
  private final T result;

  public WebResult(HttpStatus status) {
    this.status = status;
    this.result = null;
  }

  public WebResult(HttpStatus status, T result) {
    this.status = status;
    this.result = result;
  }

  public WebResult(T result) {
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
  public T getResult() {
    return result;
  }

}
