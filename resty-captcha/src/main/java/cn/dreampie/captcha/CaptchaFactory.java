package cn.dreampie.captcha;

import cn.dreampie.captcha.background.BackgroundFactory;
import cn.dreampie.captcha.color.ColorFactory;
import cn.dreampie.captcha.filter.FilterFactory;
import cn.dreampie.captcha.filter.predefined.*;
import cn.dreampie.captcha.font.FontFactory;
import cn.dreampie.captcha.font.RandomFontFactory;
import cn.dreampie.captcha.service.Captcha;
import cn.dreampie.captcha.service.ConfigurableCaptchaService;
import cn.dreampie.captcha.text.render.TextRenderer;
import cn.dreampie.captcha.word.WordFactory;

/**
 * Created by Dreampie on 16/1/6.
 */
public class CaptchaFactory {

  private ConfigurableCaptchaService captchaService = null;

  public CaptchaFactory() {
    captchaService = new ConfigurableCaptchaService();
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
        filterFactory = new CurvesRippleFilterFactory();
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

  public FilterFactory getFilterFactory() {
    return captchaService.getFilterFactory();
  }

  public CaptchaFactory setFilterFactory(FilterFactory filterFactory) {
    captchaService.setFilterFactory(filterFactory);
    return this;
  }

  /**
   * 滤镜效果
   *
   * @param captchaFilter
   * @return
   */
  public CaptchaFactory setFilterFactory(CaptchaFilter captchaFilter) {
    captchaService.setFilterFactory(getFilterFactory(captchaFilter));
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
    if (backgroundFactory.getColorFactory() == null) {
      backgroundFactory.setColorFactory(captchaService.getColorFactory());
    }
    captchaService.setBackgroundFactory(backgroundFactory);
    return this;
  }

  public ColorFactory getColorFactory() {
    return captchaService.getColorFactory();
  }

  public CaptchaFactory setColorFactory(ColorFactory colorFactory) {
    captchaService.setColorFactory(colorFactory);
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

  public CaptchaFactory setWordFactory(WordFactory wordFactory) {
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
