package cn.dreampie.captcha;

import cn.dreampie.captcha.background.BackgroundFactory;
import cn.dreampie.captcha.color.SingleColorFactory;
import cn.dreampie.captcha.filter.FilterFactory;
import cn.dreampie.captcha.filter.predefined.*;
import cn.dreampie.captcha.font.FontFactory;
import cn.dreampie.captcha.font.RandomFontFactory;
import cn.dreampie.captcha.service.Captcha;
import cn.dreampie.captcha.service.ConfigurableCaptchaService;
import cn.dreampie.captcha.text.renderer.BestFitTextRenderer;
import cn.dreampie.captcha.text.renderer.TextRenderer;
import cn.dreampie.captcha.word.RandomWordFactory;
import cn.dreampie.captcha.word.WordFactory;
import cn.dreampie.log.Logger;

import java.awt.*;

/**
 * Created by Dreampie on 16/1/6.
 */
public class CaptchaFactory {

  private Logger logger = Logger.getLogger(CaptchaFactory.class);
  private String captchaKey = null;
  private ConfigurableCaptchaService captchaService = null;

  public CaptchaFactory() {
    this("captcha");
  }

  public CaptchaFactory(String captchaKey) {
    this.captchaKey = captchaKey;
    if (captchaService == null) {
      captchaService = new ConfigurableCaptchaService();

      // 颜色创建工厂
      captchaService.setColorFactory(new SingleColorFactory(new Color(0, 0, 0)));

      // 图片滤镜设置
      captchaService.setFilterFactory(getFilterFactory(CaptchaFilter.Curves));

      // 随机字体生成器
      RandomFontFactory fontFactory = new RandomFontFactory();
      fontFactory.setMaxSize(45);
      fontFactory.setMinSize(45);
      captchaService.setFontFactory(fontFactory);

      // 随机字符生成器,去除掉容易混淆的字母和数字,如o和0等
      RandomWordFactory wordFactory = new RandomWordFactory();
      wordFactory.setCharacters("ABCDEFGHIJKLMNPQRSTUVWXYZ123456789");
      wordFactory.setMaxLength(4);
      wordFactory.setMinLength(4);
      captchaService.setWordFactory(wordFactory);

      // 文字渲染器设置
      BestFitTextRenderer textRenderer = new BestFitTextRenderer();
      textRenderer.setBottomMargin(1);
      textRenderer.setTopMargin(1);
      captchaService.setTextRenderer(textRenderer);

      captchaService.setBackgroundFactory(new RandomColorBackgroundFactory());
    }
  }

  public Captcha getCaptcha() {
    return captchaService.getCaptcha();
  }

  /**
   * 获取滤镜效果
   *
   * @param captchaFilter
   * @return
   */
  private FilterFactory getFilterFactory(CaptchaFilter captchaFilter) {
    FilterFactory filterFactory = null;
    switch (captchaFilter) {
      case Curves:
        filterFactory = new CurvesRippleFilterFactory(captchaService.getColorFactory());
        break;
      case Marble:
        filterFactory = new MarbleRippleFilterFactory();
        break;
      case Double:
        filterFactory = new DoubleRippleFilterFactory();
        break;
      case Wobble:
        filterFactory = new WobbleRippleFilterFactory();
        break;
      case Diffuse:
        filterFactory = new DiffuseRippleFilterFactory();
        break;
    }
    return filterFactory;
  }


  /**
   * 验证码key
   *
   * @return
   */
  public String getCaptchaKey() {
    return captchaKey;
  }

  public CaptchaFactory setCaptchaKey(String captchaKey) {
    this.captchaKey = captchaKey;
    return this;
  }

  public FilterFactory getFilterFactory() {
    return captchaService.getFilterFactory();
  }

  public CaptchaFactory setFilterFactory(FilterFactory filterFactory) {
    captchaService.setFilterFactory(filterFactory);
    return this;
  }

  /**
   * 背景生成工厂
   *
   * @return
   */
  public BackgroundFactory getBackgroundFactory() {
    return captchaService.getBackgroundFactory();
  }

  public CaptchaFactory setBackgroundFactory(BackgroundFactory backgroundFactory) {
    captchaService.setBackgroundFactory(backgroundFactory);
    return this;
  }

  public Color getDrawColor() {
    return captchaService.getColorFactory().getColor(0);
  }

  public CaptchaFactory setDrawColor(final Color drawColor) {
    captchaService.setColorFactory(new SingleColorFactory(drawColor));
    return this;
  }

  /**
   * 字体生成工厂
   *
   * @return
   */
  public FontFactory getFontFactory() {
    return captchaService.getFontFactory();
  }

  public CaptchaFactory setFontFactory(RandomFontFactory fontFactory) {
    captchaService.setFontFactory(fontFactory);
    return this;
  }

  /**
   * 字符生成工厂
   *
   * @return
   */
  public WordFactory getWordFactory() {
    return captchaService.getWordFactory();
  }

  public CaptchaFactory setWordFactory(RandomWordFactory wordFactory) {
    captchaService.setWordFactory(wordFactory);
    return this;
  }

  /**
   * 文本位置
   *
   * @return
   */
  public TextRenderer getTextRenderer() {
    return captchaService.getTextRenderer();
  }

  public CaptchaFactory setTextRenderer(TextRenderer textRenderer) {
    captchaService.setTextRenderer(textRenderer);
    return this;
  }

  /**
   * 验证码图片的大小
   */
  public CaptchaFactory setImgSize(int width, int height) {
    captchaService.setWidth(width);
    captchaService.setHeight(height);
    return this;
  }
}
