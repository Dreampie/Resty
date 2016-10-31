package cn.dreampie.common;

import cn.dreampie.common.http.result.HttpStatus;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;

/**
 * Date: 2/6/13
 * Time: 9:46 PM
 */
public interface Response {
  HttpStatus getStatus();

  Response setStatus(HttpStatus status);

  /**
   * Sets the content type of this response.
   * <p/>
   * It is highly recommended to call this before writing the response content, especially if you want to use getWriter().
   * <p/>
   * The response charset may be set when calling this, either to the provided charset in the content type,
   * or by default to UTF-8 if it is a 'text' content type.
   *
   * @param s the content type
   * @return the current response
   */
  Response setContentType(String s);

  /**
   * Returns the charset set on this response if any.
   * <p/>
   * The charset is set when calling setContentType, either to the provided charset in the content type,
   * or by default to UTF-8 if it is a 'text' content type.
   *
   * @return the  charset
   */
  Charset getCharset();

  /**
   * A writer you can write to to send response as text.
   * <p/>
   * The charset used is the one returned by getCharset(), or UTF-8 if not set.
   * <p/>
   * It is strongly recommended to call setContentType to set the charset before calling this method.
   *
   * @return a PrintWriter which can be used to write the response.
   * @throws java.io.IOException
   */
  PrintWriter getWriter() throws IOException;

  OutputStream getOutputStream() throws IOException;

  Response addCookie(Cookie cookie);

  Response addCookie(String name, String value);

  Response addCookie(String name, String value, int expires);

  Response clearCookie(String cookie);

  Response setHeader(String name, String value);

  /**
   * Returns the value of a header previously set with setHeader().
   *
   * @param value the name of the header to get.
   * @return the header value.
   */
  String getHeader(String value);


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

  boolean isClosed();

}
