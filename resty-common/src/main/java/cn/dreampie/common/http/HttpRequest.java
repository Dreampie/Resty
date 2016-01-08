package cn.dreampie.common.http;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * Date: 1/22/13
 * Time: 2:52 PM
 */
public class HttpRequest extends AbstractRequest {
  private final HttpServletRequest request;
  private final ServletContext servletContext;
  private Map<String, List<String>> queryParams;

  public HttpRequest(HttpServletRequest request, ServletContext servletContext) {
    this.request = request;
    this.servletContext = servletContext;
  }

  private static String getCookieValue(Cookie[] cookies, String name) {
    if (cookies == null) {
      return null;
    }
    for (Cookie cookie : cookies) {
      if (name.equals(cookie.getName()))
        return cookie.getValue();
    }
    return null;
  }

  static Cookie getCookie(Cookie[] cookies, String name) {
    if (cookies == null) {
      return null;
    }
    for (Cookie cookie : cookies) {
      if (name.equals(cookie.getName()))
        return cookie;
    }
    return null;
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
    String requestURI = request.getRequestURI();
    if (basepath.length() > 0) {
      requestURI = request.getRequestURI().substring(basepath.length());
    }
    int index = requestURI.toLowerCase().indexOf(";jsessionid=");
    if (index != -1) {
      requestURI = requestURI.substring(0, index);
    }
    try {
      return URLDecoder.decode(requestURI, getCharacterEncoding());
    } catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException("Invalid character encoding for '" + getCharacterEncoding() + "'");
    }
  }

  public String getRealPath(String path) {
    return servletContext.getRealPath(path);
  }

  public String getRestUri() {
    String queryString = getQueryString();
    if (queryString == null) {
      return getRestPath();
    } else {
      return getRestPath() + "?" + queryString;
    }
  }

  public String getQueryParam(String param) {
    return request.getParameter(param);
  }

  public List<String> getQueryParams(String param) {
    String[] values = request.getParameterValues(param);
    if (values != null)
      return Arrays.asList(values);
    else
      return null;
  }

  public Map<String, List<String>> getQueryParams() {
    if (queryParams == null) {
      Map<String, String[]> paramMap = getParamMap();
      Map<String, List<String>> params = new HashMap<String, List<String>>();
      for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
        params.put(entry.getKey(), Arrays.asList(entry.getValue()));
      }
      queryParams = params;
    }
    return queryParams;
  }

  protected Map<String, String[]> getParamMap() {
    return request.getParameterMap();
  }

  public int getContentLength() {
    return request.getContentLength();
  }

  public InputStream getContentStream() throws IOException {
    return request.getInputStream();
  }

  public String getHttpMethod() {
    String httpMethod = request.getMethod();
    if (httpMethod == null) {
      throw new IllegalArgumentException("Invalid HTTP Method for " + getRestPath());
    }
    return httpMethod;
  }

  public Map<String, String> getCookiesMap() {
    Map<String, String> cookies = new LinkedHashMap<String, String>();
    Cookie[] requestCookies = request.getCookies();
    if (requestCookies != null) {
      for (Cookie cookie : requestCookies) {
        cookies.put(cookie.getName(), cookie.getValue());
      }
    }
    return cookies;
  }

  public String getCookieValue(String name) {
    return getCookieValue(request.getCookies(), name);
  }

  public boolean isPersistentCookie(String cookie) {
    Cookie c = getCookie(request.getCookies(), cookie);
    return c != null && c.getMaxAge() > 0;
  }

  public String getQueryString() {
    try {
      String queryString = request.getQueryString();
      if (queryString != null && !"".equals(queryString)) {
        return URLDecoder.decode(request.getQueryString(), getCharacterEncoding());
      } else {
        return queryString;
      }
    } catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException("Invalid character encoding for '" + getCharacterEncoding() + "'");
    }
  }

  public RequestDispatcher getRequestDispatcher(String url) {
    return request.getRequestDispatcher(url);
  }

  public String getHeader(String name) {
    return request.getHeader(name);
  }

  public Enumeration<String> getHeaders(String name) {
    return request.getHeaders(name);
  }

  public Map<String, String> getHeaders() {
    Map<String, String> map = new HashMap<String, String>();
    Enumeration headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String key = (String) headerNames.nextElement();
      String value = request.getHeader(key);
      map.put(key, value);
    }
    return map;
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
