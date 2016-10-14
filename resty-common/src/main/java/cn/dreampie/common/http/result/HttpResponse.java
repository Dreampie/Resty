package cn.dreampie.common.http.result;

import java.util.Map;

/**
 * Created by ice on 14-12-19.
 * A HttpResponse can be raised to make resty return immediately an HTTP response with a specific HTTP status.
 */
public class HttpResponse<T> {

  private final HttpStatus status;
  private final T result;
  private final Map<String, String> headers;

  public HttpResponse(HttpStatus status) {
    this(status, null, null);
  }

  public HttpResponse(HttpStatus status, Map<String, String> headers) {
    this(status, null, headers);
  }

  public HttpResponse(T result) {
    this(HttpStatus.OK, result, null);
  }

  public HttpResponse(T result, Map<String, String> headers) {
    this(HttpStatus.OK, result, headers);
  }

  public HttpResponse(HttpStatus status, T result) {
    this(status, result, null);
  }

  public HttpResponse(HttpStatus status, T result, Map<String, String> headers) {
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
