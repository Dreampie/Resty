package cn.dreampie.captcha.background;

import cn.dreampie.captcha.color.ColorFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Created by wangrenhui on 13-12-31.
 */
public class RandomColorBackgroundFactory implements BackgroundFactory {
  private Random random = new Random();
  private int noiseNum;//噪点数量
  private int lineNum;//干扰线数量

  private Color bgColor;//背景色
  private ColorFactory colorFactory;//干扰元素颜色 null 使用随机色

  public RandomColorBackgroundFactory() {
  }

  public RandomColorBackgroundFactory(ColorFactory colorFactory) {
    this(null, colorFactory);
  }

  public RandomColorBackgroundFactory(Color bgColor, ColorFactory colorFactory) {
    this(bgColor, colorFactory, 50, 1);
  }

  public RandomColorBackgroundFactory(ColorFactory colorFactory, int noiseNum, int lineNum) {
    this(null, colorFactory, noiseNum, lineNum);
  }

  public RandomColorBackgroundFactory(Color bgColor, ColorFactory colorFactory, int noiseNum, int lineNum) {
    this.bgColor = bgColor;
    this.colorFactory = colorFactory;
    this.noiseNum = noiseNum;
    this.lineNum = lineNum;
  }


  public void fillBackground(BufferedImage image) {
    Graphics2D graphics = (Graphics2D) image.getGraphics();

    // 验证码图片的宽高
    int imgWidth = image.getWidth();
    int imgHeight = image.getHeight();

    if (bgColor != null) {
      for (int x = 0; x < imgWidth; x++) {
        for (int y = 0; y < imgHeight; y++) {
          image.setRGB(x, y, bgColor.getRGB());
        }
      }
    }

    Color drawColor = null;
    if (colorFactory == null) {
      drawColor = getRandomColor();
    } else {
      drawColor = colorFactory.getColor(0);
    }

    if (noiseNum > 0) {
      // 画100个噪点(颜色及位置随机)
      for (int i = 0; i < noiseNum; i++) {
        graphics.setColor(drawColor);
        // 随机位置
        int xInt = random.nextInt(imgWidth - 3);
        int yInt = random.nextInt(imgHeight - 2);

        // 随机旋转角度
        int sAngleInt = random.nextInt(360);
        int eAngleInt = random.nextInt(360);

        // 随机大小
        int wInt = random.nextInt(8);
        int hInt = random.nextInt(8);

        graphics.fillArc(xInt, yInt, wInt, hInt, sAngleInt, eAngleInt);
      }
    }

    // 画干扰线
    if (lineNum > 0) {
      for (int i = 0; i < lineNum; i++) {
        // 随机位置
        int xInt = random.nextInt(imgWidth - 3);
        int yInt = random.nextInt(imgHeight - 2);
        int xInt2 = random.nextInt(imgWidth);
        int yInt2 = random.nextInt(imgHeight);
        graphics.setColor(drawColor);
        graphics.setStroke(new BasicStroke(2 + 2 * random.nextFloat()));
        graphics.drawLine(xInt, yInt, xInt2, yInt2);
      }
    }

    graphics.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
    int cp = 4 + random.nextInt(3);
    int[] xPoints = new int[cp];
    int[] yPoints = new int[cp];
    imgWidth -= 10;
    for (int i = 0; i < cp; i++) {
      xPoints[i] = (int) ((int) 5 + (i * imgWidth) / (cp - 1));
      yPoints[i] = (int) (imgHeight * (0.2 + random.nextDouble() * 0.5));
    }
    int subsections = 6;
    int[] xPointsSpline = new int[(cp - 1) * subsections];
    int[] yPointsSpline = new int[(cp - 1) * subsections];
    for (int i = 0; i < cp - 1; i++) {
      double x0 = i > 0 ? xPoints[i - 1] : 2 * xPoints[i] - xPoints[i + 1];
      double x1 = xPoints[i];
      double x2 = xPoints[i + 1];
      double x3 = (i + 2 < cp) ? xPoints[i + 2] : 2 * xPoints[i + 1] - xPoints[i];
      double y0 = i > 0 ? yPoints[i - 1] : 2 * yPoints[i] - yPoints[i + 1];
      double y1 = yPoints[i];
      double y2 = yPoints[i + 1];
      double y3 = (i + 2 < cp) ? yPoints[i + 2] : 2 * yPoints[i + 1] - yPoints[i];
      for (int j = 0; j < subsections; j++) {
        xPointsSpline[i * subsections + j] = (int) catmullRomSpline(x0, x1, x2, x3, 1.0 / subsections * j);
        yPointsSpline[i * subsections + j] = (int) catmullRomSpline(y0, y1, y2, y3, 1.0 / subsections * j);
      }
    }
    for (int i = 0; i < xPointsSpline.length - 1; i++) {
      graphics.setColor(drawColor);
      graphics.setStroke(new BasicStroke(2 + 2 * random.nextFloat()));
      graphics.drawLine(xPointsSpline[i], yPointsSpline[i], xPointsSpline[i + 1], yPointsSpline[i + 1]);
    }
  }

  private double hermiteSpline(double x1, double a1, double x2, double a2, double t) {
    double t2 = t * t;
    double t3 = t2 * t;
    double b = -a2 - 2.0 * a1 - 3.0 * x1 + 3.0 * x2;
    double a = a2 + a1 + 2.0 * x1 - 2.0 * x2;
    return a * t3 + b * t2 + a1 * t + x1;
  }

  private double catmullRomSpline(double x0, double x1, double x2, double x3, double t) {
    double a1 = (x2 - x0) / 2;
    double a2 = (x3 - x1) / 2;
    return hermiteSpline(x1, a1, x2, a2, t);
  }

  public Color getRandomColor() {
    int[] c = new int[3];
    int i = random.nextInt(c.length);
    for (int fi = 0; fi < c.length; fi++) {
      if (fi == i) {
        c[fi] = random.nextInt(71);
      } else {
        c[fi] = random.nextInt(256);
      }
    }
    return new Color(c[0], c[1], c[2]);
  }

  public Color getBgColor() {
    return bgColor;
  }

  public void setBgColor(Color bgColor) {
    this.bgColor = bgColor;
  }

  public ColorFactory getColorFactory() {
    return colorFactory;
  }

  public void setColorFactory(ColorFactory colorFactory) {
    this.colorFactory = colorFactory;
  }

  public int getNoiseNum() {
    return noiseNum;
  }

  public void setNoiseNum(int noiseNum) {
    this.noiseNum = noiseNum;
  }
}