package cn.dreampie.common.http;

import cn.dreampie.common.Constant;
import cn.dreampie.common.Request;
import cn.dreampie.common.util.Joiner;

import javax.servlet.RequestDispatcher;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Date: 15/11/13
 * Time: 18:38
 */
public abstract class AbstractRequest implements Request {

  protected AbstractRequest() {
  }


  public String toString() {
    StringBuilder sb = new StringBuilder("[resty request] ");
    sb.append(getHttpMethod()).append(" ").append(getRestPath());
    dumpParams(sb);
    return sb.toString();
  }

  private void dumpParams(StringBuilder sb) {
    Map<String, List<String>> queryParams = getQueryParams();
    if (queryParams.isEmpty()) {
      return;
    }
    sb.append("?");
    for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
      String key = entry.getKey();
      List<String> values = entry.getValue();
      sb.append(key).append("=").append(
          values.size() == 1
              ? values.get(0)
              : Joiner.on("&" + key + "=").join(values));
      sb.append("&");
    }
    sb.setLength(sb.length() - 1);
  }

  public String getBaseUri() {
    return getScheme() + ":" + getBaseNetworkPath();
  }

  public String getBaseNetworkPath() {
    checkProxyRequest();
    return "//" + getHost() + getBasePath();
  }

  protected String getHost() {

    String forwardedHost = getHeader("X-Forwarded-Host");
    if (forwardedHost != null) {
      String[] hosts = forwardedHost.split(",");
      return hosts.length > 0 ? hosts[0] : getHeader("Host");
    } else {
      return getHeader("Host");
    }
  }


  public boolean isSecured() {
    checkProxyRequest();
    return getScheme().equalsIgnoreCase("https");
  }

  protected String getScheme() {
    String via = getHeader("Via");
    if (via != null) {
      boolean secured = via.toUpperCase(Locale.ENGLISH).startsWith("HTTPS");
      return secured ? "https" : "http";
    } else {
      return getLocalScheme();
    }
  }


  public String getClientAddress() {
    // see http://en.wikipedia.org/wiki/X-Forwarded-For
    checkProxyRequest();
    String xff = getHeader("X-Forwarded-For");
    if (xff != null) {
      String[] xffs = xff.split(",");
      return xffs.length > 0 ? xffs[0] : getLocalClientAddress();
    } else {
      return getLocalClientAddress();
    }
  }

  protected void checkProxyRequest() {
    if (getHeader("X-Forwarded-Proto") != null) {
      String localClientAddress = getLocalClientAddress();
      if (Constant.xForwardedSupport != null) {
        List<String> forwardedSupportes = Arrays.asList(Constant.xForwardedSupport.split(","));
        if (forwardedSupportes.size() <= 0 || (!forwardedSupportes.contains("*") && !forwardedSupportes.contains(localClientAddress))) {
          throw new IllegalArgumentException(
              "Unauthorized proxy request from " + localClientAddress + "\n" +
                  "If you are the application developer or operator, you can set `app.xForwardedSupport`\n" +
                  "application.properties property to allow proxy requests from this proxy IP with:\n" +
                  "  app.xForwardedSupport=" + localClientAddress + "\n" +
                  "Or if you want to allow any proxy request:\n" +
                  "  app.xForwardedSupport=*");
        }
      }
    }
  }

  protected abstract String getQueryString();

  protected abstract RequestDispatcher getRequestDispatcher(String url);

  /**
   * Returns the client address of this request, without taking proxy into account
   *
   * @return the client address of this request, without taking proxy into account
   */
  protected abstract String getLocalClientAddress();


  /**
   * The path on which resty is mounted.
   * Eg /api
   *
   * @return the path on which resty is mounted.
   */
  protected abstract String getBasePath();

  /**
   * The URL scheme used for this request, without taking proxy into account.
   * Eg: http, https
   *
   * @return URL scheme used for this request, without taking proxy into account.
   */
  protected abstract String getLocalScheme();

}
