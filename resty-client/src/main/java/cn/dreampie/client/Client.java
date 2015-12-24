package cn.dreampie.client;

import cn.dreampie.client.exception.ClientException;
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
public class Client extends ClientConnection {

  private static final Logger logger = Logger.getLogger(Client.class);

  private ClientRequire clientRequire;

  public Client(String apiUrl) {
    super(apiUrl);
  }

  public Client(String apiUrl, String loginApi, ClientUser user) {
    this(apiUrl, new ClientRequest(loginApi, user.reserveMap()), null);
  }

  public Client(String apiUrl, ClientRequire clientRequire) {
    this(apiUrl, null, clientRequire);
  }

  public Client(String apiUrl, String loginApi, ClientUser user, ClientRequire clientRequire) {
    this(apiUrl, new ClientRequest(loginApi, user.reserveMap()), clientRequire);
  }

  public Client(String apiUrl, ClientRequest loginRequest, ClientRequire clientRequire) {
    this(apiUrl, loginRequest, null, clientRequire);
  }

  public Client(String apiUrl, ClientRequest loginRequest, ClientRequest clientRequest, ClientRequire clientRequire) {
    super(apiUrl, loginRequest, clientRequest);
    if (clientRequire != null) {
      this.clientRequire = clientRequire;
    } else {
      this.clientRequire = new ClientRequire();
    }
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
      return readResponse(httpMethod, conn);
    } catch (Exception e) {

      if (e instanceof ClientException) {
        throw (ClientException) e;
      } else {
        String message = e.getMessage();
        if (message == null) {
          Throwable cause = e.getCause();
          if (cause != null) {
            message = cause.getMessage();
          }
        }
        throw new ClientException(message, e);
      }
    } finally {
      if (conn != null) {
        clientRequestTL.remove();
        conn.disconnect();
      }
    }
  }

  private ClientResult login(String httpMethod, ClientRequest clientRequest) {
    //login
    ClientResult result = build(loginRequest).post();
    if (result.getStatus() != HttpStatus.OK) {
      throw new ClientException("Login error " + result.getStatus().getCode() + ", " + result.getResult());
    } else {
      if (clientRequest != null) {
        result = build(clientRequest).ask(httpMethod);
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
  private ClientResult readResponse(String httpMethod, HttpURLConnection conn) throws IOException {
    int httpCode = conn.getResponseCode();
    logger.debug("Connection done. The server's response code is: %s", httpCode);
    InputStream is = null;

    boolean isSuccess = httpCode < HttpURLConnection.HTTP_BAD_REQUEST;
    if (isSuccess) {
      is = conn.getInputStream();
    }

    ClientResult result = null;
    ClientRequest clientRequest = clientRequestTL.get();
    try {
      if (is == null) {
        if (httpCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
          is = conn.getErrorStream();
        }
      }

      if (is == null) {
        logger.warn("Api " + clientRequest.getRestPath() + " response is null!!");
      } else {
        //是否是下载文件
        String downloadFile = clientRequest.getDownloadFile();
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
          result = new ClientResult(HttpStatus.havingCode(httpCode), StreamReader.readFile(is, conn.getContentLength(), file, fileRenamer).getPath());
        } else {
          result = new ClientResult(HttpStatus.havingCode(httpCode), StreamReader.readString(is, clientRequest.getEncoding()));
          //重新登录情况
          if (loginRequest != null && clientRequire.relogin(result)) {
            logger.info("Relogin to server.");
            if (!clientRequest.equals(loginRequest)) {
              return login(httpMethod, clientRequest);
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
