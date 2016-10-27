package cn.dreampie.client;

import cn.dreampie.client.exception.HttpClientException;
import cn.dreampie.common.http.HttpMethod;
import cn.dreampie.common.http.result.HttpStatus;
import cn.dreampie.common.util.stream.FileRenamer;
import cn.dreampie.common.util.stream.StreamReader;
import cn.dreampie.log.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * Resty  client
 */
public class HttpClient extends HttpClientConnection {

  private static final Logger logger = Logger.getLogger(HttpClient.class);

  private HttpClientRequire httpClientRequire;

  public HttpClient(String apiUrl) {
    super(apiUrl);
  }

  public HttpClient(String apiUrl, String loginApi, HttpClientUser user) {
    this(apiUrl, new HttpClientRequest(loginApi, user.reserveMap()), null);
  }

  public HttpClient(String apiUrl, HttpClientRequire httpClientRequire) {
    this(apiUrl, null, httpClientRequire);
  }

  public HttpClient(String apiUrl, String loginApi, HttpClientUser user, HttpClientRequire httpClientRequire) {
    this(apiUrl, new HttpClientRequest(loginApi, user.reserveMap()), httpClientRequire);
  }

  public HttpClient(String apiUrl, HttpClientRequest loginRequest, HttpClientRequire httpClientRequire) {
    this(apiUrl, loginRequest, null, httpClientRequire);
  }

  public HttpClient(String apiUrl, HttpClientRequest loginRequest, HttpClientRequest httpClientRequest, HttpClientRequire httpClientRequire) {
    super(apiUrl, loginRequest, httpClientRequest);
    if (httpClientRequire != null) {
      this.httpClientRequire = httpClientRequire;
    } else {
      this.httpClientRequire = new HttpClientRequire();
    }
  }

  public HttpClient build(HttpClientRequest httpClientRequest) {
    if (httpClientRequest == null) {
      throw new HttpClientException("HttpClientRequest must not null.");
    }
    this.clientRequestTL.set(httpClientRequest);
    return this;
  }


  public HttpClientResult get() {
    return ask(HttpMethod.GET);
  }

  public HttpClientResult post() {
    return ask(HttpMethod.POST);
  }

  public HttpClientResult put() {
    return ask(HttpMethod.PUT);
  }

  public HttpClientResult patch() {
    return ask(HttpMethod.PATCH);
  }

  public HttpClientResult delete() {
    return ask(HttpMethod.DELETE);
  }

  /**
   * 执行请求 并获取返回值
   *
   * @return responseData
   */
  private HttpClientResult ask(String httpMethod) {
    HttpURLConnection conn = null;
    try {
      conn = getHttpConnection(httpMethod);
      conn.connect();
      return readResponse(httpMethod, conn);
    } catch (Exception e) {

      if (e instanceof HttpClientException) {
        throw (HttpClientException) e;
      } else {
        String message = e.getMessage();
        if (message == null) {
          Throwable cause = e.getCause();
          if (cause != null) {
            message = cause.getMessage();
          }
        }
        throw new HttpClientException(message, e);
      }
    } finally {
      if (conn != null) {
        clientRequestTL.remove();
        conn.disconnect();
      }
    }
  }

  private HttpClientResult login(String httpMethod, HttpClientRequest httpClientRequest) {
    //login
    HttpClientResult result = build(loginRequest).post();
    if (result.getStatus() != HttpStatus.OK) {
      throw new HttpClientException("Login error " + result.getStatus().getCode() + ", " + result.getResult());
    } else {
      if (httpClientRequest != null) {
        result = build(httpClientRequest).ask(httpMethod);
      }
    }
    return result;
  }

  /**
   * 读取返回值
   *
   * @param conn
   * @return
   * @throws IOException
   */
  private HttpClientResult readResponse(String httpMethod, HttpURLConnection conn) throws IOException {
    int httpCode = conn.getResponseCode();
    logger.debug("Connection done. The server's response code is: %s", httpCode);
    InputStream is = null;

    boolean isSuccess = httpCode < HttpURLConnection.HTTP_BAD_REQUEST;
    if (isSuccess) {
      is = conn.getInputStream();
    }

    HttpClientResult result = null;
    HttpClientRequest httpClientRequest = clientRequestTL.get();
    try {
      if (is == null) {
        if (httpCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
          is = conn.getErrorStream();
        }
      }

      if (is == null) {
        logger.warn("Api " + httpClientRequest.getRestPath() + " response is null!!");
      } else {
        //是否是下载文件
        String downloadFile = httpClientRequest.getDownloadFile();
        if (isSuccess && downloadFile != null) {
          File file;
          File fileOrDirectory = new File(downloadFile);
          if (fileOrDirectory.isDirectory()) {
            String fileName = null;
            String contentDisposition = conn.getHeaderField("Content-Disposition");
            if (contentDisposition != null) {
              String fileNameBefore = "filename=";
              int fileNameIndex = contentDisposition.indexOf(fileNameBefore);

              if (fileNameIndex > -1) {
                fileName = contentDisposition.substring(fileNameIndex + 9);
              }
            }
            if (fileName == null) {
              throw new HttpClientException("Server not return filename, you must set it.");
            }
            // Write it to that dir the user supplied,
            // with the filename it arrived with
            file = new File(fileOrDirectory, fileName);
          } else {
            // Write it to the file the user supplied,
            // ignoring the filename it arrived with
            file = fileOrDirectory;
          }

          FileRenamer fileRenamer = null;
          if (!httpClientRequest.isOverwrite() && renamer != null) {
            fileRenamer = renamer;
          }
          result = new HttpClientResult(HttpStatus.havingCode(httpCode), StreamReader.readFile(is, conn.getContentLength(), file, fileRenamer).getPath());
        } else {
          result = new HttpClientResult(HttpStatus.havingCode(httpCode), StreamReader.readString(is, httpClientRequest.getEncoding()));
          //重新登录情况
          if (loginRequest != null && httpClientRequire.relogin(result)) {
            logger.info("Relogin to server.");
            if (!httpClientRequest.equals(loginRequest)) {
              return login(httpMethod, httpClientRequest);
            }
          }
          logger.debug("Reading an OK (%s) response", httpCode);
        }
      }
      return result;
    } finally {
      if (is != null) {
        is.close();
      }
    }
  }


}
