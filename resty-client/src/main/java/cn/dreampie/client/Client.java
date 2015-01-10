package cn.dreampie.client;

import cn.dreampie.client.exception.ClientException;
import cn.dreampie.log.Logger;

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;

/**
 * Resty  client
 */
public class Client extends ClientConnection {

  private static final Logger logger = Logger.getLogger(Client.class);

  protected Client(ClientRequest clientRequest) {
    super(clientRequest);
  }

  public static Client newInstance(ClientRequest clientRequest) {
    return new Client(clientRequest);
  }


  public String ask() {
    HttpURLConnection conn = null;
    try {
      conn = getHttpConnection();
      conn.connect();
      return readResponse(conn);
    } catch (Exception e) {
      throw new ClientException(e);
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
  }


  private String readResponse(HttpURLConnection conn) throws IOException {
    logger.debug("Connection done. The server's response code is: %s", conn.getResponseCode());
    InputStream is = null;
    try {
      if (conn.getResponseCode() == HttpURLConnection.HTTP_OK || conn.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
        logger.debug("Reading an OK (%s) response", conn.getResponseCode());
        is = conn.getInputStream();
      } else if (conn.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
        logger.debug("Reading an Error (%s) response", conn.getResponseCode());
        is = conn.getErrorStream();
      } else if (conn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
        logger.debug("Reading a Not Found (%s) response", conn.getResponseCode());
        throw new ClientException("Page or Resource Not Found", conn.getResponseCode());
      } else if (conn.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT) {
        logger.debug("Returning a No Content (null) (%s) response", conn.getResponseCode());
        return null;
      }
      if (is == null) {
        logger.warn("InputStream is null!!");
        throw new ConnectException("Can't connect to server");
      }
      //是否是下载文件
      if (clientRequest.getDownloadFile() != null) {
        return readFile(is, conn.getContentLength());//服务器端在这种下载的情况下  返回总是大1 未知原因
      } else {
        return readString(is);
      }
    } finally {
      if (is != null) {
        is.close();
      }
    }
  }

  private String readString(InputStream is) throws IOException {
    BufferedReader rd = new BufferedReader(new InputStreamReader(is));

    StringBuilder response = new StringBuilder();
    String line;
    while ((line = rd.readLine()) != null) {
      response.append(line);
    }
    String result = response.toString();
    logger.debug("Read object in response is: %s", result);
    return result;
  }


  private String readFile(InputStream is, int contentLength) throws IOException {
    //判断文件目录是否存在 如果不存在 创建
    File file = mkDirs(clientRequest.getDownloadFile());

    if (file.exists()) {
      if (contentLength == 0) {
        logger.warn("File was found, don't download " + file.getPath());
        return file.getPath();
      }
      long start = file.length();
      //必须要使用
      RandomAccessFile out = new RandomAccessFile(file, "rw");
      out.seek(start);
      byte[] buffer = new byte[1024];
      int len = -1;
      while ((len = is.read(buffer)) != -1) {
        out.write(buffer, 0, len);
      }
      out.close();
    } else {
      //获取一个写入文件流对象
      OutputStream out = new FileOutputStream(file);
      //创建一个4*1024大小的字节数组，作为循环读取字节流的临时存储空

      byte buffer[] = new byte[4 * 1024];
      int len = -1;
      //循环读取下载的文件到buffer对象数组中
      while ((len = is.read(buffer)) != -1) {
        //把文件流写入到文件
        out.write(buffer, 0, len);
      }
      out.close();
    }
    return file.getPath();
  }

  private File mkDirs(String path) {
    File file = new File(path);
    File parent = file.getParentFile();
    if (!parent.exists()) {
      if (!parent.mkdirs()) {
        throw new ClientException("Directory " + clientRequest.getDownloadFile() + " not exists and can not create directory.");
      }
    }
    return file;
  }

}
