package cn.dreampie.client;

import cn.dreampie.common.util.Maper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

public class ClientRequest {
  private String restUrl;
  private String method;
  private String encoding;
  private Map<String, String> parameters;
  private Map<String, String> headers;
  private int connectTimeOut = 5000;
  private int readTimeOut = 5000;

  public ClientRequest(String restUrl, String method) {
    this(restUrl, method, "UTF-8");
  }

  public ClientRequest(String restUrl, String method, String encoding) {
    this(restUrl, method, encoding, Maper.<String, String>of());
  }

  public ClientRequest(String restUrl, String method, Map<String, String> parameters) {
    this(restUrl, method, "UTF-8", parameters);
  }

  public ClientRequest(String restUrl, String method, String encoding, Map<String, String> parameters) {
    this(restUrl, method, encoding, parameters,
        Maper.<String, String>of("Content-Type", "application/x-www-form-urlencoded;charset=utf-8", "User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.146 Safari/537.36"));
  }

  public ClientRequest(String restUrl, String method, String encoding, Map<String, String> parameters, Map<String, String> headers) {
    this.restUrl = restUrl;
    this.method = method;
    this.encoding = encoding;
    this.parameters = parameters;
    this.headers = headers;
  }

  public String getRestUrl() {
    return this.restUrl.trim();
  }

  public String getMethod() {
    return this.method;
  }

  public String getEncoding() {
    return this.encoding;
  }

  public Map<String, String> getParameters() {
    return this.parameters;
  }

  public Map<String, String> getHeaders() {
    return this.headers;
  }


  public ClientRequest setRestUrl(String restUrl) {
    this.restUrl = restUrl;
    return this;
  }

  public ClientRequest setMethod(String method) {
    this.method = method;
    return this;
  }

  public ClientRequest addParameter(String headerKey, String headerValue) {
    this.parameters.put(headerKey, headerValue);
    return this;
  }

  public ClientRequest addHeader(String headerKey, String headerValue) {
    this.headers.put(headerKey, headerValue);
    return this;
  }

  public ClientRequest setEncoding(String encoding) {
    this.encoding = encoding;
    return this;
  }

  public int getConnectTimeOut() {
    return connectTimeOut;
  }

  public void setConnectTimeOut(int connectTimeOut) {
    this.connectTimeOut = connectTimeOut;
  }

  public int getReadTimeOut() {
    return readTimeOut;
  }

  public void setReadTimeOut(int readTimeOut) {
    this.readTimeOut = readTimeOut;
  }

  public String getEncodedParameters() throws UnsupportedEncodingException {
    String encodedParameters = "";
    if (!this.parameters.isEmpty()) {
      Set<String> parameterKeys = this.parameters.keySet();
      boolean isFirstParameter = true;
      for (String key : parameterKeys) {
        if (isFirstParameter) {
          encodedParameters += key + "=" + URLEncoder.encode(this.parameters.get(key), this.getEncoding());
          isFirstParameter = false;
        } else {
          encodedParameters += "&" + key + "=" + URLEncoder.encode(this.parameters.get(key), this.getEncoding());
        }
      }
    }

    return encodedParameters.trim();
  }

  public String getUnEncodedParameters() {
    String parameters = "";
    if (!this.parameters.isEmpty()) {
      Set<String> parameterKeys = this.parameters.keySet();
      boolean isFirstParameter = true;
      for (String key : parameterKeys) {
        if (isFirstParameter) {
          parameters += key + "=" + this.parameters.get(key);
          isFirstParameter = false;
        } else {
          parameters += "&" + key + "=" + this.parameters.get(key);
        }
      }
    }

    return parameters.trim();
  }

  public String getEncodedUrl() throws UnsupportedEncodingException {
    String encodedUrl = this.getRestUrl();
    if (!this.parameters.isEmpty()) {
      encodedUrl += "?";
      Set<String> parameterKeys = this.parameters.keySet();
      boolean isFirstParameter = true;
      for (String key : parameterKeys) {
        if (isFirstParameter) {
          encodedUrl += key + "=" + URLEncoder.encode(this.parameters.get(key), this.getEncoding());
          isFirstParameter = false;
        } else {
          encodedUrl += "&" + key + "=" + URLEncoder.encode(this.parameters.get(key), this.getEncoding());
        }
      }
    }
    return encodedUrl.trim();
  }

  public String getUnEncodedUrl() {
    String url = this.getRestUrl();
    if (!this.parameters.isEmpty()) {
      url += "?";
      Set<String> parameterKeys = this.parameters.keySet();
      boolean isFirstParameter = true;
      for (String key : parameterKeys) {
        if (isFirstParameter) {
          url += key + "=" + this.parameters.get(key);
          isFirstParameter = false;
        } else {
          url += "&" + key + "=" + this.parameters.get(key);
        }
      }
    }
    return url.trim();
  }

}
