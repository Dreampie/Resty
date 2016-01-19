/*
 * Copyright (c) 2009 Piotr Piastucki
 * 
 * This file is part of Patchca CAPTCHA library.
 * 
 *  Patchca is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  Patchca is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Patchca. If not, see <http://www.gnu.org/licenses/>.
 */
package cn.dreampie.captcha.service;

import cn.dreampie.captcha.background.BackgroundFactory;
import cn.dreampie.captcha.color.ColorFactory;
import cn.dreampie.captcha.filter.FilterFactory;
import cn.dreampie.captcha.font.FontFactory;
import cn.dreampie.captcha.text.render.TextRenderer;
import cn.dreampie.captcha.word.WordFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public abstract class AbstractCaptchaService implements CaptchaService {

  protected FontFactory fontFactory;
  protected WordFactory wordFactory;
  protected ColorFactory colorFactory;
  protected BackgroundFactory backgroundFactory;
  protected TextRenderer textRenderer;
  protected FilterFactory filterFactory;
  protected int width;
  protected int height;

  public FontFactory getFontFactory() {
    return fontFactory;
  }

  public void setFontFactory(FontFactory fontFactory) {
    this.fontFactory = fontFactory;
  }

  public WordFactory getWordFactory() {
    return wordFactory;
  }

  public void setWordFactory(WordFactory wordFactory) {
    this.wordFactory = wordFactory;
  }

  public ColorFactory getColorFactory() {
    return colorFactory;
  }

  public void setColorFactory(ColorFactory colorFactory) {
    this.colorFactory = colorFactory;
  }

  public BackgroundFactory getBackgroundFactory() {
    return backgroundFactory;
  }

  public void setBackgroundFactory(BackgroundFactory backgroundFactory) {
    this.backgroundFactory = backgroundFactory;
  }

  public TextRenderer getTextRenderer() {
    return textRenderer;
  }

  public void setTextRenderer(TextRenderer textRenderer) {
    this.textRenderer = textRenderer;
  }

  public FilterFactory getFilterFactory() {
    return filterFactory;
  }

  public void setFilterFactory(FilterFactory filterFactory) {
    this.filterFactory = filterFactory;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }


  public Captcha getCaptcha() {
    BufferedImage bufImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    backgroundFactory.fillBackground(bufImage);
    try {
      File f = new File("/tmp/test1.png");
      ImageIO.write(bufImage, "png", f);
    } catch (Exception e) {
      e.printStackTrace();
    }

    String word = wordFactory.getNextWord();
    textRenderer.draw(word, bufImage, fontFactory, colorFactory);
    try {
      File f = new File("/tmp/test2.png");
      ImageIO.write(bufImage, "png", f);
    } catch (Exception e) {
      e.printStackTrace();
    }

    bufImage = filterFactory.applyFilters(bufImage);
    try {
      File f = new File("/tmp/test3.png");
      ImageIO.write(bufImage, "png", f);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return new Captcha(word, bufImage);
  }

}
