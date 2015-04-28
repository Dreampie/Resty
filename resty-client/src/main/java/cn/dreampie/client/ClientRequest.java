package cn.dreampie.client;

import cn.dreampie.common.util.Maper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

import static cn.dreampie.common.util.Checker.checkNotNull;

public class ClientRequest {
  private String restUrl;
  private String method;
  private String encoding;
  private Map<String, String> params = Maper.of();
  private String jsonParam;
  private Map<String, String> headers = Maper.of();
  private int connectTimeOut = 10000;
  private int readTimeOut = 10000;
  private boolean overwrite = false;
  private String downloadFile;
  private Map<String, String> uploadFiles = Maper.of();

  public ClientRequest(String restUrl, String method) {
    this(restUrl, method, "UTF-8");
  }

  public ClientRequest(String restUrl, String method, String encoding) {
    this(restUrl, method, encoding, Maper.<String, String>of());
  }

  public ClientRequest(String restUrl, String method, Map<String, String> params) {
    this(restUrl, method, "UTF-8", params);
  }

  public ClientRequest(String restUrl, String method, String encoding, Map<String, String> params) {
    this(restUrl, method, encoding, params,
        Maper.of("Content-Type", "application/x-www-form-urlencoded;charset=" + encoding, "User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.146 Safari/537.36"));
  }

  public ClientRequest(String restUrl, String method, String encoding, Map<String, String> params, Map<String, String> headers) {
    this.restUrl = checkNotNull(restUrl);
    this.method = checkNotNull(method);
    this.encoding = encoding;
    this.params = params;
    this.headers = headers;
  }

  public String getRestUrl() {
    return this.restUrl.trim();
  }

  public ClientRequest setRestUrl(String restUrl) {
    this.restUrl = restUrl;
    return this;
  }

  public String getMethod() {
    return this.method;
  }

  public ClientRequest setMethod(String method) {
    this.method = method;
    return this;
  }

  public String getEncoding() {
    return this.encoding;
  }

  public ClientRequest setEncoding(String encoding) {
    this.encoding = encoding;
    return this;
  }

  public Map<String, String> getParams() {
    return this.params;
  }

  public void setParams(Map<String, String> params) {
    this.params = params;
  }

  public Map<String, String> getHeaders() {
    return this.headers;
  }

  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }

  public ClientRequest addParam(String name, String value) {
    this.params.put(name, value);
    return this;
  }

  public String getJsonParam() {
    return jsonParam;
  }

  public void setJsonParam(String jsonParam) {
    this.addHeader("Content-Type", "application/json;charset=" + encoding);
    this.jsonParam = jsonParam;
  }

  public ClientRequest addHeader(String key, String value) {
    this.headers.put(key, value);
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

  public boolean isOverwrite() {
    return overwrite;
  }

  public void setDownloadFile(String downloadFile, boolean overwrite) {
    this.downloadFile = downloadFile;
    this.overwrite = overwrite;
  }

  public String getDownloadFile() {
    return downloadFile;
  }

  public void setDownloadFile(String downloadFile) {
    this.downloadFile = downloadFile;
  }

  public Map<String, String> getUploadFiles() {
    return uploadFiles;
  }

  public void setUploadFiles(Map<String, String> uploadFiles) {
    this.uploadFiles = uploadFiles;
  }

  public void addUploadFile(String name, String filepath) {
    this.uploadFiles.put(name, filepath);
  }

  public String getEncodedParams() throws UnsupportedEncodingException {
    String encodedParams = "";
    if (!this.params.isEmpty()) {
      Set<String> paramKeys = this.params.keySet();
      boolean isFirstParam = true;
      String value = null;
      for (String key : paramKeys) {
        value = this.params.get(key);
        if (value == null) continue;
        if (isFirstParam) {
          encodedParams += key + "=" + URLEncoder.encode(value, this.getEncoding());
          isFirstParam = false;
        } else {
          encodedParams += "&" + key + "=" + URLEncoder.encode(value, this.getEncoding());
        }
      }
    }

    return encodedParams.trim();
  }

  public String getUnEncodedParams() {
    String params = "";
    if (!this.params.isEmpty()) {
      Set<String> paramKeys = this.params.keySet();
      boolean isFirstParam = true;
      String value = null;
      for (String key : paramKeys) {
        value = this.params.get(key);
        if (value == null) continue;
        if (isFirstParam) {
          params += key + "=" + value;
          isFirstParam = false;
        } else {
          params += "&" + key + "=" + value;
        }
      }
    }

    return params.trim();
  }

  public String getEncodedUrl() throws UnsupportedEncodingException {
    String encodedUrl = this.getRestUrl();
    if (!this.params.isEmpty()) {
      encodedUrl += "?";
      Set<String> paramKeys = this.params.keySet();
      boolean isFirstParam = true;
      String value = null;
      for (String key : paramKeys) {
        value = this.params.get(key);
        if (value == null) continue;
        if (isFirstParam) {
          encodedUrl += key + "=" + URLEncoder.encode(value, this.getEncoding());
          isFirstParam = false;
        } else {
          encodedUrl += "&" + key + "=" + URLEncoder.encode(value, this.getEncoding());
        }
      }
    }
    return encodedUrl.trim();
  }

  public String getUnEncodedUrl() {
    String url = this.getRestUrl();
    if (!this.params.isEmpty()) {
      url += "?";
      Set<String> paramKeys = this.params.keySet();
      boolean isFirstParam = true;
      String value = null;
      for (String key : paramKeys) {
        value = this.params.get(key);
        if (value == null) continue;
        if (isFirstParam) {
          url += key + "=" + value;
          isFirstParam = false;
        } else {
          url += "&" + key + "=" + value;
        }
      }
    }
    return url.trim();
  }

}
