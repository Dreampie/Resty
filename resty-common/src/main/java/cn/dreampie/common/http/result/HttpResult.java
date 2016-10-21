package cn.dreampie.common.http.result;

import java.util.Map;

/**
 * Created by ice on 14-12-19.
 * A HttpResult can be raised to make resty return immediately an HTTP response with a specific HTTP status.
 */
public class HttpResult<T> {

  private final HttpStatus status;
  private final T result;
  private final Map<String, String> headers;

  public HttpResult(HttpStatus status) {
    this(status, null, null);
  }

  public HttpResult(HttpStatus status, Map<String, String> headers) {
    this(status, null, headers);
  }

  public HttpResult(T result) {
    this(HttpStatus.OK, result, null);
  }

  public HttpResult(T result, Map<String, String> headers) {
    this(HttpStatus.OK, result, headers);
  }

  public HttpResult(HttpStatus status, T result) {
    this(status, result, null);
  }

  public HttpResult(HttpStatus status, T result, Map<String, String> headers) {
    this.status = status;
    this.result = result;
    this.headers = headers;
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

  public Map<String, String> getHeaders() {
    return headers;
  }
}
