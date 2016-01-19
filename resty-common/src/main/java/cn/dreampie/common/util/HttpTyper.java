package cn.dreampie.common.util;

import cn.dreampie.common.util.properties.Proper;

import java.util.Iterator;
import java.util.Properties;

import static cn.dreampie.common.util.Checker.checkArgument;

/**
 * Created by ice on 14-12-29.
 */
public class HttpTyper {
  private static final Properties mimeType;
  private final static String RFC_2616_TOKEN_SPECIAL_CHARS_REGEX = "[\\s\\(\\)<>@,;:\\\\\"/\\[\\]\\?=\\{\\}]";

  static {
    mimeType = Proper.use("mime-types.properties").getProperties();
    for (String prop : mimeType.stringPropertyNames()) {
      Iterable<String> types = Lister.of(mimeType.getProperty(prop));
      Iterator<String> iterator = types.iterator();
      mimeType.setProperty(prop, iterator.hasNext() ? iterator.next() : "application/octet-stream");
    }
  }

  public static String getContentTypeFromFileName(String filename) {
    String ext = filename.substring(filename.lastIndexOf('.') + 1);
    return mimeType.getProperty(ext);
  }

  public static String getContentTypeFromExtension(String ext) {
    return mimeType.getProperty(ext);
  }

  public static boolean isTextContentType(String contentType) {
    // the list is not fully exhaustive, should cover most cases.
    return contentType.startsWith("text/")
        || contentType.startsWith("application/json")
        || contentType.startsWith("application/javascript")
        || contentType.startsWith("application/ecmascript")
        || contentType.startsWith("application/atom+xml")
        || contentType.startsWith("application/rss+xml")
        || contentType.startsWith("application/xhtml+xml")
        || contentType.startsWith("application/soap+xml")
        || contentType.startsWith("application/xml")
        ;
  }

  public static String charsetFromContentType(String s) {
    if (!s.contains("charset=")) {
      return null;
    } else {
      return s.substring(s.indexOf("charset=") + "charset=".length());
    }
  }

  public static String headerTokenCompatible(String s, String specialCharsReplacement) {
    checkArgument(specialCharsReplacement.replaceAll(RFC_2616_TOKEN_SPECIAL_CHARS_REGEX, "blah").equals(specialCharsReplacement),
        "specialCharsReplacement `%s` is not itself compatible with rfc 2616 !",
        specialCharsReplacement);

    // See rfc 2616 for allowed chars in header tokens (http://www.ietf.org/rfc/rfc2616.txt page 16)
    return s.replaceAll(RFC_2616_TOKEN_SPECIAL_CHARS_REGEX, specialCharsReplacement);
  }

}
