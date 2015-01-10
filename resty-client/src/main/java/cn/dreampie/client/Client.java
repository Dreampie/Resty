package cn.dreampie.client;

import cn.dreampie.client.exception.ClientException;
import cn.dreampie.log.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    BufferedReader rd = null;
    try {
      InputStream is = null;
      if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
        logger.debug("Reading an OK (%s) response", HttpURLConnection.HTTP_OK);
        is = conn.getInputStream();
      } else if (conn.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
        logger.debug("Reading an Error (%s) response", conn.getResponseCode());
        is = conn.getErrorStream();
      } else if (conn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
        logger.debug("Reading a Not Found (%s) response", conn.getResponseCode());
        throw new ClientException("Page or Resource Not Found", conn.getResponseCode());
      } else if (conn.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT) {
        logger.debug("Returning a No Content (null) (%s) response", HttpURLConnection.HTTP_NO_CONTENT);
        return null;
      }
      if (is == null) {
        logger.warn("InputStream is null!!");
        throw new ConnectException("Can't connect to server");
      }
      rd = new BufferedReader(new InputStreamReader(is));

      StringBuilder response = new StringBuilder();
      String line;
      while ((line = rd.readLine()) != null) {
        response.append(line);
      }
      String result = response.toString();
      logger.debug("Read object in response is: %s", result);
      return result;
    } finally {
      if (rd != null) {
        rd.close();
      }
    }
  }

}
