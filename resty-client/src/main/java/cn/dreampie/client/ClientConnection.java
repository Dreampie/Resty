package cn.dreampie.client;

import cn.dreampie.client.exception.ClientException;
import cn.dreampie.common.util.HttpTyper;
import cn.dreampie.common.util.stream.DefaultFileRenamer;
import cn.dreampie.common.util.stream.FileRenamer;
import cn.dreampie.log.Logger;

import javax.net.ssl.*;
import java.io.*;
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
public class ClientConnection {
  private static final Logger logger = Logger.getLogger(ClientConnection.class);

  protected ClientRequest loginRequest;
  protected ThreadLocal<ClientRequest> clientRequestTL = new ThreadLocal<ClientRequest>();
  protected CookieManager cookieManager = new CookieManager();

  protected String apiUrl;

  protected FileRenamer renamer = new DefaultFileRenamer();

  protected ClientConnection(String apiUrl) {
    this(apiUrl, null);
  }

  protected ClientConnection(String apiUrl, ClientRequest loginRequest) {
    this(apiUrl, loginRequest, null);
  }

  protected ClientConnection(String apiUrl, ClientRequest loginRequest, ClientRequest clientRequest) {
    this.apiUrl = apiUrl;
    this.loginRequest = loginRequest;
    if (clientRequest != null) {
      this.clientRequestTL.set(clientRequest);
    }
    //add cookieManager
    cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
    CookieHandler.setDefault(cookieManager);
  }

  /**
   * 文件重名规则
   *
   * @param renamer 重命名工具
   */
  public void setRenamer(FileRenamer renamer) {
    if (renamer == null) {
      throw new ClientException("FileRenamer must not null.");
    }
    this.renamer = renamer;
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

  private final SSLSocketFactory sslSocketFactory = initSSLSocketFactory();
  private final TrustAnyHostnameVerifier trustAnyHostnameVerifier = new TrustAnyHostnameVerifier();

  private SSLSocketFactory initSSLSocketFactory() {
    try {
      TrustManager[] tm = {new TrustAnyTrustManager()};
      SSLContext sslContext = SSLContext.getInstance("TLS", "SunJSSE");
      sslContext.init(null, tm, new java.security.SecureRandom());
      return sslContext.getSocketFactory();
    } catch (Exception e) {
      throw new ClientException(e.getMessage(), e);
    }
  }

  protected HttpURLConnection getHttpConnection() throws IOException, NoSuchAlgorithmException, NoSuchProviderException, KeyManagementException {
    ClientRequest clientRequest = clientRequestTL.get();

    URL _url = null;
    HttpURLConnection conn = null;
    String method = clientRequest.getMethod();
    //使用OutPutStream输出参数
    if (HttpMethod.POST.contains(method)) {
      _url = new URL(apiUrl + clientRequest.getRestUrl());
      conn = openHttpURLConnection(_url, method);

      conn.setDoOutput(true);
      conn.setUseCaches(false);
      //是上传文件
      Map<String, String> uploadFiles = clientRequest.getUploadFiles();
      if (uploadFiles != null && uploadFiles.size() > 0) {

        String boundary = "---------------------------" + getRandomString(13); //boundary就是request头和上传文件内容的分隔符
        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

        // params
        Map<String, String> params = clientRequest.getParameters();

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
        writeUploadFiles(boundary, clientRequest.getUploadFiles(), writer);

        byte[] endData = ("\r\n--" + boundary + "--\r\n").getBytes();
        writer.write(endData);
        writer.flush();
        writer.close();
      } else {
        //没有文件上传
        String contentType = clientRequest.getHeaders().get("Content-Type");
        String requestParameters;
        //application/json  传递参数
        if (contentType != null && contentType.toLowerCase().contains(HttpTyper.ContentType.JSON.value())) {
          requestParameters = clientRequest.getJsonParameter();
        } else {
          requestParameters = clientRequest.getEncodedParameters();
        }
        //写入参数
        if (requestParameters != null && !"".equals(requestParameters)) {
          DataOutputStream writer = new DataOutputStream(conn.getOutputStream());
          logger.debug("Request out method " + method + ",out parameters " + requestParameters);

          writer.writeBytes(requestParameters);
          writer.flush();
          writer.close();
        }
      }
    } else {
      _url = new URL(apiUrl + clientRequest.getEncodedUrl());
      conn = openHttpURLConnection(_url, method);
    }

    //ssl判断
    if (conn instanceof HttpsURLConnection) {
      ((HttpsURLConnection) conn).setSSLSocketFactory(sslSocketFactory);
      ((HttpsURLConnection) conn).setHostnameVerifier(trustAnyHostnameVerifier);
    }

    conn.setConnectTimeout(clientRequest.getConnectTimeOut());
    conn.setReadTimeout(clientRequest.getReadTimeOut());


    return conn;
  }

  /**
   * 写入文件到  服务器
   *
   * @param boundary 分隔符
   * @param params   文件集合
   * @param writer   写入对象
   * @throws IOException
   */
  private void writeUploadFiles(String boundary, Map<String, String> params, DataOutputStream writer) throws IOException {
    // file
    String value = null;
    for (String key : params.keySet()) {
      value = params.get(key);
      if (value == null) continue;
      File file = new File(value);
      if (!file.exists())
        throw new FileNotFoundException("File not found " + file.getPath());

      String filename = file.getName();
      String contentType = HttpTyper.getContentTypeFromExtension(filename);
      writer.write(("\r\n" + "--" + boundary + "\r\n" + "Content-Disposition: form-data; name=\"" + key + "\"; filename=\"" + filename + "\"\r\n" + "Content-Type:" + contentType + "\r\n\r\n").getBytes());

      DataInputStream in = new DataInputStream(new FileInputStream(file));
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
   * @param _url   url
   * @param method method
   * @return
   * @throws IOException
   */
  private HttpURLConnection openHttpURLConnection(URL _url, String method) throws IOException {
    logger.info("Open connection for api " + _url.getPath());

    ClientRequest clientRequest = clientRequestTL.get();

    HttpURLConnection.setFollowRedirects(true);
    HttpURLConnection conn;
    conn = (HttpURLConnection) _url.openConnection();
    conn.setRequestMethod(method);

    String downloadFile = clientRequest.getDownloadFile();
    if (downloadFile != null) {
      File file = new File(downloadFile);
      if (file.exists()) {
        //设置下载区间
        conn.setRequestProperty("RANGE", "bytes=" + file.length() + "-");
      }
    }
    Map<String, String> headers = clientRequest.getHeaders();
    if (headers != null && !headers.isEmpty())
      for (Map.Entry<String, String> entry : headers.entrySet())
        conn.setRequestProperty(entry.getKey(), entry.getValue());

    return conn;
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


}
