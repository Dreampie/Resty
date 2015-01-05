package cn.dreampie.route.http;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static cn.dreampie.util.Checker.checkArgument;

/**
 * Date: 1/22/13
 * Time: 2:52 PM
 */
public class HttpRequest extends AbstractRequest {
  private final HttpServletRequest request;
  private BufferedInputStream bufferedInputStream;
  private Map<String, List<String>> queryParams;

  public HttpRequest(HttpServletRequest request) {
    this.request = request;
  }


  public String getLocalClientAddress() {
    return request.getRemoteAddr();
  }


  protected String getBasePath() {
    return request.getContextPath();
  }


  protected String getLocalScheme() {
    return request.getScheme();
  }


  public String getRestPath() {
    String basepath = getBasePath();
    if (basepath.length() > 0)
      return request.getRequestURI().substring(basepath.length());
    else
      return request.getRequestURI();
  }


  public String getRestUri() {
    if (request.getQueryString() == null) {
      return getRestPath();
    } else {
      return getRestPath() + "?" + request.getQueryString();
    }
  }


  public String getQueryParam(String param) {
    return request.getParameter(param);
  }


  public List<String> getQueryParams(String param) {
    return Arrays.asList(request.getParameterValues(param));
  }


  public Map<String, List<String>> getQueryParams() {
    if (queryParams == null) {
      Map<String, String[]> parameterMap = getParameterMap();
      Map<String, List<String>> params = new HashMap<String, List<String>>();
      for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
        params.put(entry.getKey(), Arrays.asList(entry.getValue()));
      }
      queryParams = params;
    }
    return queryParams;
  }

  protected Map<String, String[]> getParameterMap() {
    return request.getParameterMap();
  }


  public InputStream getContentStream() throws IOException {
        /*
           maybe we could do this buffering only in dev mode?
           It is used to be able to read data again in case of json processing error.
         */
    if (bufferedInputStream == null) {
      bufferedInputStream = new BufferedInputStream(request.getInputStream()) {

        public void close() throws IOException {
          // NO OP, see #closeContentStream
        }
      };
      bufferedInputStream.mark(10 * 1024);
    }
    return bufferedInputStream;
  }


  public void closeContentStream() throws IOException {
    bufferedInputStream.close();
  }


  public String getHttpMethod() {
    String httpMethod = request.getMethod();
    checkArgument(httpMethod != null, "Invalid HTTP Method : " + httpMethod);
    return httpMethod;
  }


  public Map<String, String> getCookiesMap() {
    Map<String, String> cookies = new LinkedHashMap<String, String>();
    Cookie[] requestCookies = request.getCookies();
    if (requestCookies != null) {
      for (int i = 0; i < requestCookies.length; i++) {
        Cookie cookie = requestCookies[i];
        cookies.put(cookie.getName(), cookie.getValue());
      }
    }
    return cookies;
  }


  public String getCookieValue(String cookieName) {
    return getCookieValue(request.getCookies(), cookieName);
  }


  public boolean isPersistentCookie(String cookie) {
    Cookie c = getCookie(request.getCookies(), cookie);
    return c != null && c.getMaxAge() > 0;
  }

  private static String getCookieValue(Cookie[] cookies,
                                       String cookieName) {
    if (cookies == null) {
      return null;
    }
    for (int i = 0; i < cookies.length; i++) {
      Cookie cookie = cookies[i];
      if (cookieName.equals(cookie.getName()))
        return cookie.getValue();
    }
    return null;
  }

  static Cookie getCookie(Cookie[] cookies, String cookieName) {
    if (cookies == null) {
      return null;
    }
    for (int i = 0; i < cookies.length; i++) {
      Cookie cookie = cookies[i];
      if (cookieName.equals(cookie.getName()))
        return cookie;
    }
    return null;
  }

  public RequestDispatcher getRequestDispatcher(String url) {
    return request.getRequestDispatcher(url);
  }

  public String getHeader(String headerName) {
    return request.getHeader(headerName);
  }

  public Enumeration<String> getHeaders(String headerName) {
    return request.getHeaders(headerName);
  }


  public String getContentType() {
    return request.getContentType();
  }


  @SuppressWarnings("unchecked")
  public <T> T unwrap(Class<T> clazz) {
    if (clazz == HttpServletRequest.class || clazz == ServletRequest.class) {
      return (T) request;
    }
    throw new IllegalArgumentException("underlying implementation is HttpServletRequest, not " + clazz.getName());
  }


  public Locale getLocale() {
    return request.getLocale();
  }

  public List<Locale> getLocales() {
    return Collections.list((request.getLocales()));
  }

  public String getCharacterEncoding() {
    return request.getCharacterEncoding();
  }

  public void setCharacterEncoding(String encoding) throws UnsupportedEncodingException {
    request.setCharacterEncoding(encoding);
  }
}
