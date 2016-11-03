package cn.dreampie.common.http.result;

import javax.servlet.http.Cookie;
import java.util.List;
import java.util.Map;

/**
 * Created by ice on 14-12-19.
 * A HttpResult can be raised to make resty return immediately an HTTP response with a specific HTTP status.
 */
public class HttpResult<T> {

  private final HttpStatus status;
  private final T result;
  private final Map<String, String> headers;
  private final List<Cookie> cookies;

  public HttpResult(HttpStatus status) {
    this(status, null, null);
  }

  public HttpResult(HttpStatus status, Map<String, String> headers) {
    this(status, null, headers, null);
  }

  public HttpResult(HttpStatus status, List<Cookie> cookies) {
    this(status, null, null, cookies);
  }

  public HttpResult(T result) {
    this(HttpStatus.OK, result, null);
  }

  public HttpResult(T result, List<Cookie> cookies) {
    this(HttpStatus.OK, result, cookies);
  }

  public HttpResult(T result, Map<String, String> headers) {
    this(HttpStatus.OK, result, headers, null);
  }

  public HttpResult(T result, Map<String, String> headers, List<Cookie> cookies) {
    this(HttpStatus.OK, result, headers, cookies);
  }

  public HttpResult(HttpStatus status, T result) {
    this(status, result, null);
  }

  public HttpResult(HttpStatus status, T result, List<Cookie> cookies) {
    this(status, result, null, cookies);
  }

  public HttpResult(HttpStatus status, T result, Map<String, String> headers, List<Cookie> cookies) {
    this.status = status;
    this.result = result;
    this.headers = headers;
    this.cookies = cookies;
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

  public List<Cookie> getCookies() {
    return cookies;
  }
}
