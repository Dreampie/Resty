package cn.dreampie.client;

import cn.dreampie.client.exception.ClientException;
import cn.dreampie.log.Logger;

import javax.net.ssl.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

/**
 * Created by wangrenhui on 15/1/10.
 */
public class ClientConnection {
  private static final Logger logger = Logger.getLogger(ClientConnection.class);


  protected ClientRequest clientRequest;

  protected ClientConnection(ClientRequest clientRequest) {
    super();
    this.clientRequest = clientRequest;
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
      throw new ClientException(e);
    }
  }

  protected HttpURLConnection getHttpConnection() throws IOException, NoSuchAlgorithmException, NoSuchProviderException, KeyManagementException {
    URL _url = null;
    HttpURLConnection conn = null;
    String method = clientRequest.getMethod();
    //使用OutPutStream输出参数
    if (HttpMethod.OUT_METHODS.contains(method)) {
      _url = new URL(clientRequest.getRestUrl());
      conn = openHttpURLConnection(_url, method);

      conn.setDoOutput(true);

      String requestParameters = clientRequest.getEncodedParameters();
      logger.debug("Request out method " + method + ",out parameters " + requestParameters);

      DataOutputStream writer = new DataOutputStream(conn.getOutputStream());
      writer.writeBytes(requestParameters);
      writer.flush();
      writer.close();
    } else {
      _url = new URL(clientRequest.getEncodedUrl());
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

  private HttpURLConnection openHttpURLConnection(URL _url, String method) throws IOException {
    HttpURLConnection conn;
    conn = (HttpURLConnection) _url.openConnection();
    conn.setRequestMethod(method);

    Map<String, String> headers = clientRequest.getHeaders();
    if (headers != null && !headers.isEmpty())
      for (Map.Entry<String, String> entry : headers.entrySet())
        conn.setRequestProperty(entry.getKey(), entry.getValue());
    return conn;
  }

}
