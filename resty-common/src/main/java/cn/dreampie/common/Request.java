package cn.dreampie.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Date: 1/22/13
 * Time: 2:49 PM
 */
public interface Request {
  /**
   * Returns the base URI of this request.
   * Eg http://mydomain.com/api or http://mydomain.com:8080
   * <p/>
   * When used behind a proxy, this will try to return the client facing URI, by using:
   * - X-Forwarded-Host for the host
   * - X-Forwarded-Proto for the scheme
   * - checking first Via to know if request was made in HTTPS
   * <p/>
   * see http://en.wikipedia.org/wiki/X-Forwarded-For
   * see http://en.wikipedia.org/wiki/List_of_HTTP_header_fields
   * see http://httpd.apache.org/docs/current/mod/mod_proxy.html#proxyvia
   *
   * @return the base URI of this request.
   */
  String getBaseUri();

  /**
   * Returns the base network path of this request (ie baseUri without the protocol).
   * Eg //mydomain.com/api or //mydomain.com:8080
   * <p/>
   * This is useful to create paths using the same protocol as the one seen by the client, as opposed
   * to the protocol seen by the server (server can see http if you have a front http server like Apache
   * doing https and reverse proxy).
   * <p/>
   * See also this discussion:
   * http://stackoverflow.com/questions/5799577/does-using-www-example-com-in-javascript-chose-http-https-protocol-automatical
   * <p/>
   * Note that if Via headers are set getBaseUri should be fine too.
   *
   * @return the base network path of this request.
   */
  String getBaseNetworkPath();

  /**
   * Returns the resty portion of the request path.
   * <p>
   * If incoming request is http://mydomain.com/api/myresource/test?q=test and baseUri is http://mydomain.com/api,
   * then resty path will be /myresource/test
   * </p>
   *
   * @return the resty portion of the request path.
   */
  String getRestPath();


  /**
   * Returns the resty portion of the webRoot  physical path.
   *
   * @param path webRoot relative path
   * @return the resty portion of the  webRoot  physical path.
   */
  String getRealPath(String path);

  /**
   * Returns the resty portion of the full request uri.
   * <p>
   * If incoming request is http://mydomain.com/api/myresource/test?q=test and baseUri is http://mydomain.com/api,
   * then resty uri will be /myresource/test?q=test
   * </p>
   *
   * @return the resty portion of the full request uri.
   */
  String getRestUri();

  /**
   * Is this request performed through a secured connection or not.
   * <p/>
   * This will return true if:
   * - the HttpSettings proto() is set to 'https'
   * - the request has a 'X-Forwarded-Proto' header with value 'https', and comes from an authorized proxy
   * as defined by HttpSettings.forwardedSupport()
   * - the request was performed in HTTPS on this server
   *
   * @return true if this request is performed through a secured (HTTPS) connection.
   */
  boolean isSecured();

  /**
   * HTTP METHOD, eg GET, POST, ...
   *
   * @return the request HTTP method
   */
  String getHttpMethod();

  String getQueryParam(String param);

  List<String> getQueryParams(String param);

  Map<String, List<String>> getQueryParams();

  String getHeader(String name);

  Enumeration<String> getHeaders(String name);

  Map<String,String> getHeaders();

  String getContentType();

  String getCookieValue(String name);

  boolean isPersistentCookie(String cookie);

  Map<String, String> getCookiesMap();

  /**
   * The address (IP) of the client.
   * <p/>
   * If X-Forwarded-For header is present, it will return its value, otherwise it returns
   * the remote client address.
   * <p/>
   * see http://httpd.apache.org/docs/current/mod/mod_proxy.html#x-headers for details on this header.
   *
   * @return IP address of the client.
   */
  String getClientAddress();

  int getContentLength();

  InputStream getContentStream() throws IOException;

  /**
   * Unwraps the underlying native implementation of given class.
   * <p/>
   * Examnple: This is a HttpServletRequest in a servlet container.
   *
   * @param clazz the class of the underlying implementation
   * @param <T>   unwrapped class
   * @return the unwrapped implementation.
   * @throws IllegalArgumentException if the underlying implementation is not of given type.
   */
  <T> T unwrap(Class<T> clazz);

  /**
   * Returns the preferred <code>Locale</code> that the client will
   * accept content in, based on the Accept-Language header.
   * If the client request doesn't provide an Accept-Language header,
   * this method returns the default locale for the server.
   *
   * @return the preferred <code>Locale</code> for the client
   */
  Locale getLocale();

  /**
   * Returns an <code>ImmutableList</code> of <code>Locale</code> objects
   * indicating, in decreasing order starting with the preferred locale, the
   * locales that are acceptable to the client based on the Accept-Language
   * header.
   * If the client request doesn't provide an Accept-Language header,
   * this method returns an <code>ImmutableList</code> containing one
   * <code>Locale</code>, the default locale for the server.
   *
   * @return an <code>ImmutableList</code> of preferred
   * <code>Locale</code> objects for the client
   */
  List<Locale> getLocales();

  String getCharacterEncoding();

  void setCharacterEncoding(String var1) throws UnsupportedEncodingException;
}
