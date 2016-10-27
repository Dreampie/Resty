package cn.dreampie.client;

import cn.dreampie.client.exception.HttpClientException;
import cn.dreampie.common.http.ContentType;
import cn.dreampie.common.http.HttpMethod;
import cn.dreampie.common.util.stream.DefaultFileRenamer;
import cn.dreampie.common.util.stream.FileRenamer;
import cn.dreampie.log.Logger;

import javax.net.ssl.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Random;

/**
 * Created by wangrenhui on 15/1/10.
 */
public class HttpClientConnection {
  private static final Logger logger = Logger.getLogger(HttpClientConnection.class);
  private final SSLSocketFactory sslSocketFactory = initSSLSocketFactory();
  private final TrustAnyHostnameVerifier trustAnyHostnameVerifier = new TrustAnyHostnameVerifier();
  protected HttpClientRequest loginRequest;
  protected ThreadLocal<HttpClientRequest> clientRequestTL = new ThreadLocal<HttpClientRequest>();
  protected CookieManager cookieManager = new CookieManager();
  protected String apiUrl;
  protected FileRenamer renamer = new DefaultFileRenamer();

  protected HttpClientConnection(String apiUrl) {
    this(apiUrl, null);
  }

  protected HttpClientConnection(String apiUrl, HttpClientRequest loginRequest) {
    this(apiUrl, loginRequest, null);
  }

  protected HttpClientConnection(String apiUrl, HttpClientRequest loginRequest, HttpClientRequest httpClientRequest) {
    this.apiUrl = apiUrl;
    this.loginRequest = loginRequest;
    if (httpClientRequest != null) {
      this.clientRequestTL.set(httpClientRequest);
    }else{
      this.clientRequestTL.set(new HttpClientRequest());
    }
    //add cookieManager
    cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
    CookieHandler.setDefault(cookieManager);
  }

  public static String getRandomString(int length) { //length表示生成字符串的长度
    String base = "abcdefghijklmnopqrstuvwxyz0123456789";
    Random random = new Random();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < length; i++) {
      int number = random.nextInt(base.length());
      sb.append(base.charAt(number));
    }
    return sb.toString();
  }

  /**
   * 文件重命名工具
   *
   * @param renamer 重命名工具
   */
  public void setRenamer(FileRenamer renamer) {
    if (renamer == null) {
      throw new HttpClientException("FileRenamer must not null.");
    }
    this.renamer = renamer;
  }

  private SSLSocketFactory initSSLSocketFactory() {
    try {
      TrustManager[] tm = {new TrustAnyTrustManager()};
      SSLContext sslContext = SSLContext.getInstance("TLS", "SunJSSE");
      sslContext.init(null, tm, new java.security.SecureRandom());
      return sslContext.getSocketFactory();
    } catch (Exception e) {
      throw new HttpClientException(e.getMessage(), e);
    }
  }

  /**
   * 获取Connection
   *
   * @param httpMethod
   * @return
   * @throws IOException
   * @throws NoSuchAlgorithmException
   * @throws NoSuchProviderException
   * @throws KeyManagementException
   */
  protected HttpURLConnection getHttpConnection(String httpMethod) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, KeyManagementException {
    HttpClientRequest httpClientRequest = clientRequestTL.get();

    URL url = null;
    HttpURLConnection conn = null;

    String contentType = httpClientRequest.getContentType();
    //json请求
    if (contentType.contains(ContentType.JSON.value())) {
      // 除了post意外其他请求类型都用 拼接参数方式
      if (httpMethod.equals(HttpMethod.GET) || httpMethod.equals(HttpMethod.DELETE)) {
        if (!"".equals(httpClientRequest.getJsonParam())) {
          url = new URL(apiUrl + httpClientRequest.getEncodedRestPath() + "?" + httpClientRequest.getEncodedJsonParam());
        } else {
          url = new URL(apiUrl + httpClientRequest.getEncodedRestPath());
        }
        conn = openHttpURLConnection(url, httpClientRequest, httpMethod);
      } else {
        conn = getStreamConnection(httpMethod, httpClientRequest);
        outputParam(conn, httpMethod, httpClientRequest.getJsonParam(), httpClientRequest.getEncoding());
      }
    } else if (contentType.contains(ContentType.MULTIPART.value()) || httpMethod.equals(HttpMethod.POST)) {
      //上传文件类型
      conn = getStreamConnection(httpMethod, httpClientRequest);
      //是上传文件
      Map<String, HttpClientFile> uploadFiles = httpClientRequest.getUploadFiles();
      if (uploadFiles != null && uploadFiles.size() > 0) {
        String boundary = "---------------------------" + getRandomString(13); //boundary就是request头和上传文件内容的分隔符
        conn.setRequestProperty("Content-Type", contentType + "boundary=" + boundary);

        // params
        Map<String, String> params = httpClientRequest.getParams();

        DataOutputStream writer = new DataOutputStream(conn.getOutputStream());
        if (params != null && params.size() > 0) {
          StringBuilder builder = new StringBuilder();

          String value = null;
          for (String key : params.keySet()) {
            value = params.get(key);
            if (value == null) continue;
            builder.append("\r\n").append("--").append(boundary).append("\r\n");
            builder.append("Content-Disposition: form-data; name=\"").append(key).append("\"\r\n\r\n");
            builder.append(value);
          }
          writer.write(builder.toString().getBytes());
        }
        //上传文件
        writeUploadFiles(boundary, uploadFiles, writer);

        byte[] endData = ("\r\n--" + boundary + "--\r\n").getBytes();
        writer.write(endData);
        writer.flush();
        writer.close();
      } else {
        outputParam(conn, httpMethod, httpClientRequest.getEncodedParams(), httpClientRequest.getEncoding());
      }
    } else {
      url = new URL(apiUrl + httpClientRequest.getEncodedUrl());
      conn = openHttpURLConnection(url, httpClientRequest, httpMethod);
    }

    //ssl判断
    if (conn instanceof HttpsURLConnection) {
      ((HttpsURLConnection) conn).setSSLSocketFactory(sslSocketFactory);
      ((HttpsURLConnection) conn).setHostnameVerifier(trustAnyHostnameVerifier);
    }

    conn.setConnectTimeout(httpClientRequest.getConnectTimeOut());
    conn.setReadTimeout(httpClientRequest.getReadTimeOut());
    return conn;
  }

  /**
   * outConnection
   *
   * @param httpMethod
   * @param httpClientRequest
   * @return
   * @throws IOException
   */
  private HttpURLConnection getStreamConnection(String httpMethod, HttpClientRequest httpClientRequest) throws IOException {
    URL url;
    HttpURLConnection conn;
    url = new URL(apiUrl + httpClientRequest.getEncodedRestPath());
    conn = openHttpURLConnection(url, httpClientRequest, httpMethod);

    conn.setDoOutput(true);
    conn.setUseCaches(false);
    return conn;
  }

  /**
   * 输出参数
   *
   * @param conn
   * @param method
   * @param requestParams
   * @throws IOException
   */
  private void outputParam(HttpURLConnection conn, String method, String requestParams, String encoding) throws IOException {
    //写入参数
    if (requestParams != null && !"".equals(requestParams)) {
      DataOutputStream writer = new DataOutputStream(conn.getOutputStream());
      logger.debug("Request out method " + method + ",out parameters " + requestParams);

      writer.write(requestParams.getBytes(encoding));
      writer.flush();
      writer.close();
    }
  }

  /**
   * 写入文件到  服务器
   *
   * @param boundary    分隔符
   * @param uploadFiles 文件集合
   * @param writer      写入对象
   * @throws IOException
   */
  private void writeUploadFiles(String boundary, Map<String, HttpClientFile> uploadFiles, DataOutputStream writer) throws IOException {
    // file
    HttpClientFile httpClientFile;
    for (String key : uploadFiles.keySet()) {
      httpClientFile = uploadFiles.get(key);
      if (httpClientFile == null) continue;

      writer.write(("\r\n" + "--" + boundary + "\r\n" + "Content-Disposition: form-data; name=\"" + key + "\"; filename=\"" + httpClientFile.getName() + "\"\r\n" + "Content-Type:" + httpClientFile.getContentType() + "\r\n\r\n").getBytes());

      DataInputStream in = new DataInputStream(httpClientFile.getInputStream());
      int bytes = 0;
      byte[] bufferOut = new byte[1024];
      while ((bytes = in.read(bufferOut)) != -1) {
        writer.write(bufferOut, 0, bytes);
      }
      in.close();
    }
  }

  /**
   * open a  Connection
   *
   * @param url    url
   * @param method method
   * @return
   * @throws IOException
   */
  private HttpURLConnection openHttpURLConnection(URL url, HttpClientRequest httpClientRequest, String method) throws IOException {
    logger.info("Open connection for api " + url.getPath());

    HttpURLConnection.setFollowRedirects(true);
    HttpURLConnection conn;
    conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod(method);

    String downloadFile = httpClientRequest.getDownloadFile();
    if (downloadFile != null) {
      File file = new File(downloadFile);
      if (file.exists()) {
        //设置下载区间
        conn.setRequestProperty("RANGE", "bytes=" + file.length() + "-");
      }
    }
    Map<String, String> headers = httpClientRequest.getHeaders();
    if (headers != null && !headers.isEmpty()) {
      for (Map.Entry<String, String> entry : headers.entrySet()) {
        conn.setRequestProperty(entry.getKey(), entry.getValue());
      }
    }
    logger.info("Hold cookie: %s.", cookieManager.getCookieStore().getCookies());
    conn.setRequestProperty("Content-Type", httpClientRequest.getContentType());
    conn.setRequestProperty("User-Agent", httpClientRequest.getUserAgent());
    return conn;
  }

  /**
   * https 域名校验
   */
  private class TrustAnyHostnameVerifier implements HostnameVerifier {
    public boolean verify(String hostname, SSLSession session) {
      return true;
    }
  }

  /**
   * https 证书管理
   */
  private class TrustAnyTrustManager implements X509TrustManager {
    public X509Certificate[] getAcceptedIssuers() {
      return null;
    }

    public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
    }

    public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
    }
  }


}
