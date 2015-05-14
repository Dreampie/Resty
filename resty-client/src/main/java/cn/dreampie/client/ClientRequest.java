package cn.dreampie.client;

import cn.dreampie.common.http.ContentType;
import cn.dreampie.common.http.Encoding;
import cn.dreampie.common.util.Checker;
import cn.dreampie.common.util.Maper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

import static cn.dreampie.common.util.Checker.checkNotNull;

public class ClientRequest {
  private String restUrl;
  private String encoding = Encoding.UTF_8.toString();
  private Map<String, String> params = Maper.of();
  private String jsonParam;
  private Map<String, String> headers = Maper.of();
  private int connectTimeOut = 10000;
  private int readTimeOut = 10000;
  private boolean overwrite = false;
  private String downloadFile;
  private Map<String, String> uploadFiles = Maper.of();
  private String contentType = ContentType.FORM + ";charset=" + encoding;
  private String userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.146 Safari/537.36";


  public ClientRequest(String restUrl) {
    this(restUrl, Encoding.UTF_8.toString());
  }

  public ClientRequest(String restUrl, String encoding) {
    this(restUrl, encoding, Maper.<String, String>of());
  }

  public ClientRequest(String restUrl, Map<String, String> params) {
    this(restUrl, null, params);
  }

  public ClientRequest(String restUrl, String encoding, Map<String, String> params) {
    this(restUrl, encoding, params, Maper.<String, String>of());
  }

  public ClientRequest(String restUrl, String encoding, Map<String, String> params, Map<String, String> headers) {
    this.restUrl = checkNotNull(restUrl);
    if (encoding != null) {
      this.encoding = encoding;
    }
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

  public ClientRequest setParams(Map<String, String> params) {
    this.params = params;
    return this;
  }

  public Map<String, String> getHeaders() {
    return this.headers;
  }

  public ClientRequest setHeaders(Map<String, String> headers) {
    this.headers = headers;
    return this;
  }

  public ClientRequest addParam(String name, String value) {
    this.params.put(name, value);
    return this;
  }

  public String getContentType() {
    return contentType;
  }

  public ClientRequest setContentType(String contentType) {
    this.contentType = contentType;
    return this;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public ClientRequest setUserAgent(String userAgent) {
    this.userAgent = userAgent;
    return this;
  }

  public String getJsonParam() {
    return jsonParam;
  }

  public ClientRequest setJsonParam(String jsonParam) {
    setContentType(ContentType.JSON + ";charset=" + encoding);
    this.jsonParam = checkNotNull(jsonParam, "Json param could not be null.");
    return this;
  }

  public ClientRequest addHeader(String key, String value) {
    this.headers.put(key, value);
    return this;
  }

  public int getConnectTimeOut() {
    return connectTimeOut;
  }

  public ClientRequest setConnectTimeOut(int connectTimeOut) {
    this.connectTimeOut = connectTimeOut;
    return this;
  }

  public int getReadTimeOut() {
    return readTimeOut;
  }

  public ClientRequest setReadTimeOut(int readTimeOut) {
    this.readTimeOut = readTimeOut;
    return this;
  }

  public boolean isOverwrite() {
    return overwrite;
  }

  public ClientRequest setDownloadFile(String downloadFile, boolean overwrite) {
    this.downloadFile = Checker.checkNotNull(downloadFile, "Download file could not be null.");
    this.overwrite = overwrite;
    setContentType(ContentType.MULTIPART + ";charset=" + encoding);
    return this;
  }

  public String getDownloadFile() {
    return downloadFile;
  }

  public ClientRequest setDownloadFile(String downloadFile) {
    setDownloadFile(downloadFile, false);
    return this;
  }

  public Map<String, String> getUploadFiles() {
    return uploadFiles;
  }

  public ClientRequest setUploadFiles(Map<String, String> uploadFiles) {
    this.uploadFiles = uploadFiles;
    return this;
  }

  public ClientRequest addUploadFile(String name, String filepath) {
    this.uploadFiles.put(name, filepath);
    return this;
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
