package cn.dreampie.client;

import cn.dreampie.client.exception.ClientException;
import cn.dreampie.common.http.HttpMethod;
import cn.dreampie.common.http.result.HttpStatus;
import cn.dreampie.common.util.Maper;
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
public class Client extends ClientConnection {

  private static final Logger logger = Logger.getLogger(Client.class);


  public Client(String apiUrl) {
    super(apiUrl);
  }

  public Client(String apiUrl, String loginApi, String username, String password) {
    super(apiUrl, new ClientRequest(loginApi, HttpMethod.POST, Maper.of("username", username, "password", password)));
  }

  public Client(String apiUrl, String loginApi, String username, String password, boolean rememberMe) {
    super(apiUrl, new ClientRequest(loginApi, HttpMethod.POST, Maper.of("username", username, "password", password, "rememberMe", Boolean.toString(rememberMe))));
  }

  public Client(String apiUrl, String loginApi, String usernamePara, String username, String passwordPara, String password) {
    super(apiUrl, new ClientRequest(loginApi, HttpMethod.POST, Maper.of(usernamePara, username, passwordPara, password)));
  }

  public Client(String apiUrl, String loginApi, String usernamePara, String username, String passwordPara, String password, String rememberMePara, boolean rememberMe) {
    super(apiUrl, new ClientRequest(loginApi, HttpMethod.POST, Maper.of(usernamePara, username, passwordPara, password, rememberMePara, Boolean.toString(rememberMe))));
  }

  public Client build(ClientRequest clientRequest) {
    if (clientRequest == null) {
      throw new ClientException("ClientRequest must not null.");
    }
    this.clientRequestTL.set(clientRequest);
    return this;
  }


  public ClientResult get() {
    return ask(HttpMethod.GET);
  }

  public ClientResult post() {
    return ask(HttpMethod.POST);
  }

  public ClientResult put() {
    return ask(HttpMethod.PUT);
  }

  public ClientResult patch() {
    return ask(HttpMethod.PATCH);
  }

  public ClientResult delete() {
    return ask(HttpMethod.DELETE);
  }

  /**
   * 执行请求 并获取返回值
   *
   * @return responseData
   */
  private ClientResult ask(String httpMethod) {
    HttpURLConnection conn = null;
    try {
      conn = getHttpConnection(httpMethod);
      conn.connect();
      return readResponse(conn);
    } catch (Exception e) {
      Throwable cause = e.getCause();
      if (cause != null) {
        throwException(cause);
      } else {
        throwException(e);
      }
      return null;
    } finally {
      if (conn != null) {
        clientRequestTL.remove();
        conn.disconnect();
      }
    }
  }

  /**
   * 抛出异常
   *
   * @param cause
   */
  private void throwException(Throwable cause) {
    if (cause instanceof ClientException) {
      throw (ClientException) cause;
    } else {
      throw new ClientException(cause.getMessage(), cause);
    }
  }


  private ClientResult login(ClientRequest clientRequest) {
    //login
    ClientResult result = build(loginRequest).post();
    if (result.getStatus() != HttpStatus.OK) {
      throw new ClientException("Login error " + result.getStatus() + ", " + result.getResult());
    } else {
      if (clientRequest != null)
        result = build(clientRequest).post();
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
  private ClientResult readResponse(HttpURLConnection conn) throws IOException {
    int httpCode = conn.getResponseCode();
    logger.debug("Connection done. The server's response code is: %s", httpCode);
    InputStream is = null;

    ClientRequest clientRequest = clientRequestTL.get();
    try {
      if (httpCode == HttpURLConnection.HTTP_OK || httpCode == HttpURLConnection.HTTP_PARTIAL) {
        logger.debug("Reading an OK (%s) response", httpCode);
        is = conn.getInputStream();
      } else if (httpCode == HttpURLConnection.HTTP_NOT_FOUND) {
        logger.debug("Reading a Not Found (%s) response", httpCode);
        throw new ClientException("Resource Not Found", httpCode);
      } else if (httpCode == HttpURLConnection.HTTP_NO_CONTENT) {
        logger.debug("Returning a No Content (null) (%s) response", httpCode);
        return null;
      } else if (loginRequest != null && httpCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
        logger.info("Relogin to server.");
        if (!clientRequest.equals(loginRequest))
          return login(clientRequest);
      }
      if (is == null) {
        is = conn.getErrorStream();
        if (is == null) {
          logger.warn("Api " + clientRequest.getRestUrl() + " response is null!!");
        }
      } else {
        //是否是下载文件
        String downloadFile = clientRequest.getDownloadFile();
        if (downloadFile != null) {
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
              throw new ClientException("Server not return filename, you must set it.");
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
          if (!clientRequest.isOverwrite() && renamer != null) {
            fileRenamer = renamer;
          }
          return new ClientResult(HttpStatus.havingCode(httpCode), StreamReader.readFile(is, conn.getContentLength(), file, fileRenamer).getPath());
        }
      }
      return new ClientResult(HttpStatus.havingCode(httpCode), StreamReader.readString(is));
    } finally {
      if (is != null) {
        is.close();
      }
    }
  }


}
