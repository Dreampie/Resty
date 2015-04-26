package cn.dreampie.common;

import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.common.http.exception.WebException;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Created by ice on 14-12-29.
 */
public abstract class Render {

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
      throw new WebException(e.getMessage());
    }
  }

  public void write(HttpRequest request, HttpResponse response, String type, RenderedImage content) {
    OutputStream outputStream = null;
    try {
      outputStream = response.getOutputStream();
      ImageIO.write(content, type, outputStream);
    } catch (Exception e) {
      throw new WebException(e.getMessage());
    }
  }
}
