package cn.dreampie.route.handler.cors;

import cn.dreampie.common.http.HttpMethod;
import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.common.http.exception.WebException;
import cn.dreampie.common.http.result.HttpStatus;
import cn.dreampie.common.util.Joiner;
import cn.dreampie.common.util.Lister;
import cn.dreampie.log.Logger;
import cn.dreampie.route.handler.Handler;

import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ice on 14-12-22.
 */
public class CORSHandler extends Handler {
  public static final String ACCESS_CONTROL_REQUEST_METHOD_HEADER = "Access-Control-Request-Method";
  public static final String ACCESS_CONTROL_REQUEST_HEADERS_HEADER = "Access-Control-Request-Headers";
  // Response headers
  public static final String ACCESS_CONTROL_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";
  public static final String ACCESS_CONTROL_ALLOW_METHODS_HEADER = "Access-Control-Allow-Methods";
  public static final String ACCESS_CONTROL_ALLOW_HEADERS_HEADER = "Access-Control-Allow-Headers";
  public static final String ACCESS_CONTROL_MAX_AGE_HEADER = "Access-Control-Max-Age";
  public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER = "Access-Control-Allow-Credentials";
  public static final String ACCESS_CONTROL_EXPOSE_HEADERS_HEADER = "Access-Control-Expose-Headers";
  private static final Logger logger = Logger.getLogger(CORSHandler.class);
  // Request headers
  private static final String ORIGIN_HEADER = "Origin";
  // Implementation constants
  private static final List<String> SIMPLE_HTTP_METHODS = Lister.of(HttpMethod.GET, HttpMethod.POST, HttpMethod.HEAD);

  private boolean anyOriginAllowed = true;
  private boolean anyHeadersAllowed = false;
  private List<String> allowedOrigins = Lister.of("*");
  private List<String> allowedMethods = Lister.of(HttpMethod.GET, HttpMethod.POST, HttpMethod.HEAD);
  private List<String> allowedHeaders = Lister.of("X-Requested-With", "Content-Type", "Accept", "Origin");
  private List<String> exposedHeaders = null;
  private int preflightMaxAge = 1800;
  private boolean allowCredentials = true;
  private boolean chainPreflight = true;

  public CORSHandler() {
  }

  public CORSHandler(String allowedMethods) {
    this(null, allowedMethods, null);
  }

  public CORSHandler(String allowedMethods, String allowedHeaders) {
    this(null, allowedMethods, allowedHeaders);
  }

  public CORSHandler(String allowedOrigins, String allowedMethods, String allowedHeaders) {
    this(allowedOrigins, allowedMethods, allowedHeaders, null);
  }

  /**
   * @param allowedOrigins Multiple origins allowed, separated default *
   * @param allowedMethods Multiple httpMethods allowed, separated default GET,POST,HEAD
   * @param allowedHeaders Multiple headers allowed, separated default X-Requested-With,Content-Type,Accept,Origin
   * @param exposedHeaders Multiple origins expose, separated default null
   */
  public CORSHandler(String allowedOrigins, String allowedMethods, String allowedHeaders, String exposedHeaders) {
    if (allowedOrigins != null)
      this.allowedOrigins = Lister.of(allowedOrigins.split(","));
    if (allowedMethods != null)
      this.allowedMethods = Lister.of(allowedMethods.split(","));
    if (allowedHeaders != null)
      this.allowedHeaders = Lister.of(allowedHeaders.split(","));
    if (exposedHeaders != null)
      this.exposedHeaders = Lister.of(exposedHeaders.split(","));
  }

  public final void handle(HttpRequest request, HttpResponse response, boolean[] isHandled) {
    String origin = request.getHeader(ORIGIN_HEADER);
    // Is it a cross origin request ?
    if (origin != null && isEnabled(request)) {
      if (originMatches(origin)) {
        if (isSimpleRequest(request)) {
          logger.debug("Cross-origin request to %s is a simple cross-origin request", request.getRestPath());
          handleSimpleResponse(request, response, origin);
        } else if (isPreflightRequest(request)) {
          logger.debug("Cross-origin request to %s is a preflight cross-origin request", request.getRestPath());
          handlePreflightResponse(request, response, origin);
          if (chainPreflight)
            logger.debug("Preflight cross-origin request to %s forwarded to application", request.getRestPath());
          else
            throw new WebException(HttpStatus.FORBIDDEN, "Unauthorized CORS request");
        } else {
          logger.debug("Cross-origin request to %s is a non-simple cross-origin request", request.getRestPath());
          handleSimpleResponse(request, response, origin);
        }
      } else {
        logger.debug("Cross-origin request to " + request.getRestPath() + " with origin " + origin + " does not match allowed origins " + allowedOrigins);
      }
    }
    nextHandler.handle(request, response, isHandled);
  }

  protected boolean isEnabled(HttpRequest request) {
    // WebSocket clients such as Chrome 5 implement a version of the WebSocket
    // protocol that does not accept extra response headers on the upgrade response
    for (Enumeration connections = request.getHeaders("Connection"); connections.hasMoreElements(); ) {
      String connection = (String) connections.nextElement();
      if ("Upgrade".equalsIgnoreCase(connection)) {
        for (Enumeration upgrades = request.getHeaders("Upgrade"); upgrades.hasMoreElements(); ) {
          String upgrade = (String) upgrades.nextElement();
          if ("WebSocket".equalsIgnoreCase(upgrade))
            return false;
        }
      }
    }
    return true;
  }

  private boolean originMatches(String originList) {
    if (anyOriginAllowed)
      return true;

    if (originList.trim().length() == 0)
      return false;

    String[] origins = originList.split(" ");
    for (String origin : origins) {
      if (origin.trim().length() == 0)
        continue;

      for (String allowedOrigin : allowedOrigins) {
        if (allowedOrigin.contains("*")) {
          Matcher matcher = createMatcher(origin, allowedOrigin);
          if (matcher.matches())
            return true;
        } else if (allowedOrigin.equals(origin)) {
          return true;
        }
      }
    }
    return false;
  }

  private Matcher createMatcher(String origin, String allowedOrigin) {
    String regex = parseAllowedWildcardOriginToRegex(allowedOrigin);
    Pattern pattern = Pattern.compile(regex);
    return pattern.matcher(origin);
  }

  private String parseAllowedWildcardOriginToRegex(String allowedOrigin) {
    String regex = allowedOrigin.replace(".", "\\.");
    return regex.replace("*", ".*"); // we want to be greedy here to match multiple subdomains, thus we use .*
  }

  private boolean isSimpleRequest(HttpRequest request) {

    if (SIMPLE_HTTP_METHODS.contains(request.getHttpMethod())) {
      // TODO: implement better detection of simple headers
      // The specification says that for a request to be simple, custom request headers must be simple.
      // Here for simplicity I just check if there is a Access-Control-Request-Method header,
      // which is required for preflight requests
      return request.getHeader(ACCESS_CONTROL_REQUEST_METHOD_HEADER) == null;
    }
    return false;
  }

  private boolean isPreflightRequest(HttpRequest request) {
    if (HttpMethod.OPTIONS.equalsIgnoreCase(request.getHttpMethod())) {
      return true;
    }
    if (request.getHeader(ACCESS_CONTROL_REQUEST_METHOD_HEADER) == null) {
      return false;
    }
    return true;
  }

  private void handleSimpleResponse(HttpRequest request, HttpResponse response, String origin) {
    response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, origin);
    //W3C CORS spec http://www.w3.org/TR/cors/#resource-implementation
    if (!anyOriginAllowed)
      response.addHeader("Vary", ORIGIN_HEADER);
    if (allowCredentials)
      response.setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
    if (exposedHeaders != null && !exposedHeaders.isEmpty())
      response.setHeader(ACCESS_CONTROL_EXPOSE_HEADERS_HEADER, Joiner.on(",").join(exposedHeaders));
  }

  private void handlePreflightResponse(HttpRequest request, HttpResponse response, String origin) {
    boolean methodAllowed = isMethodAllowed(request);

    if (!methodAllowed)
      return;
    List<String> headersRequested = getAccessControlRequestHeaders(request);
    boolean headersAllowed = areHeadersAllowed(headersRequested);
    if (!headersAllowed)
      return;
    response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, origin);
    //W3C CORS spec http://www.w3.org/TR/cors/#resource-implementation
    if (!anyOriginAllowed)
      response.addHeader("Vary", ORIGIN_HEADER);
    if (allowCredentials)
      response.setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
    if (preflightMaxAge > 0)
      response.setHeader(ACCESS_CONTROL_MAX_AGE_HEADER, String.valueOf(preflightMaxAge));
    response.setHeader(ACCESS_CONTROL_ALLOW_METHODS_HEADER, Joiner.on(",").join(allowedMethods));
    if (anyHeadersAllowed)
      response.setHeader(ACCESS_CONTROL_ALLOW_HEADERS_HEADER, Joiner.on(",").join(headersRequested));
    else
      response.setHeader(ACCESS_CONTROL_ALLOW_HEADERS_HEADER, Joiner.on(",").join(allowedHeaders));
  }

  private boolean isMethodAllowed(HttpRequest request) {
    String accessControlRequestMethod = request.getHeader(ACCESS_CONTROL_REQUEST_METHOD_HEADER);
    logger.debug("%s is %s", ACCESS_CONTROL_REQUEST_METHOD_HEADER, accessControlRequestMethod);
    boolean result = false;
    if (accessControlRequestMethod != null)
      result = allowedMethods.contains(accessControlRequestMethod);
    logger.debug("Method %s is" + (result ? "" : " not") + " among allowed methods %s", accessControlRequestMethod, allowedMethods);
    return result;
  }

  List<String> getAccessControlRequestHeaders(HttpRequest request) {
    String accessControlRequestHeaders = request.getHeader(ACCESS_CONTROL_REQUEST_HEADERS_HEADER);
    logger.debug("%s is %s", ACCESS_CONTROL_REQUEST_HEADERS_HEADER, accessControlRequestHeaders);
    if (accessControlRequestHeaders == null)
      return Lister.of();

    List<String> requestedHeaders = Lister.of();
    String[] headers = accessControlRequestHeaders.split(",");
    for (String header : headers) {
      String h = header.trim();
      if (h.length() > 0)
        requestedHeaders.add(h);
    }
    return requestedHeaders;
  }


  private boolean areHeadersAllowed(List<String> requestedHeaders) {
    if (anyHeadersAllowed) {
      logger.debug("Any header is allowed");
      return true;
    }

    boolean result = true;
    for (String requestedHeader : requestedHeaders) {
      boolean headerAllowed = false;
      for (String allowedHeader : allowedHeaders) {
        if (requestedHeader.equalsIgnoreCase(allowedHeader.trim())) {
          headerAllowed = true;
          break;
        }
      }
      if (!headerAllowed) {
        result = false;
        break;
      }
    }
    logger.debug("Headers [%s] are" + (result ? "" : " not") + " among allowed headers %s", requestedHeaders, allowedHeaders);
    return result;
  }


  public List<String> getAllowedOrigins() {
    return allowedOrigins;
  }

  public void setAllowedOrigins(String... allowedOrigins) {
    if (allowedOrigins.length == 1 && allowedOrigins[0].equals("*")) {
      this.anyOriginAllowed = true;
    }
    this.allowedOrigins = Lister.of(allowedOrigins);
  }

  public List<String> getAllowedMethods() {
    return allowedMethods;
  }

  public void setAllowedMethods(String... allowedMethods) {
    this.allowedMethods = Lister.of(allowedMethods);
  }

  public List<String> getAllowedHeaders() {
    return allowedHeaders;
  }

  public void setAllowedHeaders(String... allowedHeaders) {
    if (allowedHeaders.length == 1 && allowedHeaders[0].equals("*")) {
      this.anyHeadersAllowed = true;
    }
    this.allowedHeaders = Lister.of(allowedHeaders);
  }

  public List<String> getExposedHeaders() {
    return exposedHeaders;
  }

  public void setExposedHeaders(String... exposedHeaders) {
    this.exposedHeaders = Lister.of(exposedHeaders);
  }

  public int getPreflightMaxAge() {
    return preflightMaxAge;
  }

  public void setPreflightMaxAge(int preflightMaxAge) {
    this.preflightMaxAge = preflightMaxAge;
  }

  public boolean isAllowCredentials() {
    return allowCredentials;
  }

  public void setAllowCredentials(boolean allowCredentials) {
    this.allowCredentials = allowCredentials;
  }

  public boolean isChainPreflight() {
    return chainPreflight;
  }

  public void setChainPreflight(boolean chainPreflight) {
    this.chainPreflight = chainPreflight;
  }

}
