package cn.dreampie.common;

import cn.dreampie.common.http.HttpMessage;
import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.common.http.exception.HttpException;
import cn.dreampie.log.Logger;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Created by ice on 14-12-29.
 */
public abstract class Render {

  public static final Logger logger = Logger.getLogger(Render.class);

  /**
   * Render to client
   */
  public abstract void render(HttpRequest request, HttpResponse response, Object out);

  public void write(HttpRequest request, HttpResponse response, String content) {
    PrintWriter writer = null;
    try {
      writer = response.getWriter();
      writer.print(content);
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
      throw new HttpException(HttpMessage.INTERNAL_SERVER_ERROR);
    }
  }

  public void write(HttpRequest request, HttpResponse response, String type, RenderedImage content) {
    OutputStream outputStream = null;
    try {
      outputStream = response.getOutputStream();
      ImageIO.write(content, type, outputStream);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      throw new HttpException(HttpMessage.INTERNAL_SERVER_ERROR);
    }
  }
}
