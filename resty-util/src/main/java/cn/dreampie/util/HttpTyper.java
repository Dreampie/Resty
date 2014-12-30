package cn.dreampie.util;

import static cn.dreampie.util.Checker.checkArgument;

/**
 * Created by ice on 14-12-29.
 */
public class HttpTyper {
  private final static String RFC_2616_TOKEN_SPECIAL_CHARS_REGEX = "[\\s\\(\\)<>@,;:\\\\\"/\\[\\]\\?=\\{\\}]";

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

  public static enum ContentType {

    TEXT("text/plain"),
    HTML("text/html"),
    XML("text/xml"),
    JSON("application/json"),
    JAVASCRIPT("application/javascript");

    private final String value;

    private ContentType(String value) {
      this.value = value;
    }

    public String value() {
      return value;
    }

    public String toString() {
      return value;
    }
  }
}
