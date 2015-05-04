package cn.dreampie.common.http;

import java.nio.charset.Charset;

/**
 * Created by ice on 14-12-29.
 */
public class Encoding {
  /**
   * US-ASCII: seven-bit ASCII, the Basic Latin block of the Unicode character set (ISO646-US).
   * <p/>
   * <p><b>Note for Java 7 and later:</b> this constant should be treated as deprecated; use
   * {@link sun.nio.cs.StandardCharsets#aliases_US_ASCII} instead.
   */
  public static final Charset US_ASCII = Charset.forName("US-ASCII");
  /**
   * ISO-8859-1: ISO Latin Alphabet Number 1 (ISO-LATIN-1).
   * <p/>
   * <p><b>Note for Java 7 and later:</b> this constant should be treated as deprecated; use
   * {@link sun.nio.cs.StandardCharsets#aliases_ISO_8859_1} instead.
   */
  public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
  /**
   * UTF-8: eight-bit UCS Transformation Format.
   * <p/>
   * <p><b>Note for Java 7 and later:</b> this constant should be treated as deprecated; use
   * {@link sun.nio.cs.StandardCharsets#aliases_UTF_8} instead.
   */
  public static final Charset UTF_8 = Charset.forName("UTF-8");
  /**
   * UTF-16BE: sixteen-bit UCS Transformation Format, big-endian byte order.
   * <p/>
   * <p><b>Note for Java 7 and later:</b> this constant should be treated as deprecated; use
   * {@link sun.nio.cs.StandardCharsets#aliases_UTF_16BE} instead.
   */
  public static final Charset UTF_16BE = Charset.forName("UTF-16BE");
  /**
   * UTF-16LE: sixteen-bit UCS Transformation Format, little-endian byte order.
   * <p/>
   * <p><b>Note for Java 7 and later:</b> this constant should be treated as deprecated; use
   * {@link sun.nio.cs.StandardCharsets#aliases_UTF_16LE} instead.
   */
  public static final Charset UTF_16LE = Charset.forName("UTF-16LE");
  /**
   * UTF-16: sixteen-bit UCS Transformation Format, byte order identified by an optional byte-order
   * mark.
   * <p/>
   * <p><b>Note for Java 7 and later:</b> this constant should be treated as deprecated; use
   * {@link sun.nio.cs.StandardCharsets#aliases_UTF_16} instead.
   */
  public static final Charset UTF_16 = Charset.forName("UTF-16");

  private Encoding() {
  }

}
