/*
 * Copyright (c) 2009 Piotr Piastucki
 * 
 * This file is part of Patchca captcha.patchca library.
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
package cn.dreampie.captcha.text.render;

import cn.dreampie.captcha.color.ColorFactory;
import cn.dreampie.captcha.font.FontFactory;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public abstract class AbstractTextRender implements TextRenderer {

  protected int leftMargin;
  protected int rightMargin;
  protected int topMargin;
  protected int bottomMargin;

  public AbstractTextRender() {
    this(10, 5);
  }

  public AbstractTextRender(int leftRightMargin, int topBottomMargin) {
    this(leftRightMargin, leftRightMargin, topBottomMargin, topBottomMargin);
  }

  public AbstractTextRender(int leftMargin, int rightMargin, int topMargin, int bottomMargin) {
    this.leftMargin = leftMargin;
    this.rightMargin = rightMargin;
    this.topMargin = topMargin;
    this.bottomMargin = bottomMargin;
  }

  protected abstract void arrangeCharacters(int width, int height, TextString ts);

  public void setLeftMargin(int leftMargin) {
    this.leftMargin = leftMargin;
  }


  public void setRightMargin(int rightMargin) {
    this.rightMargin = rightMargin;
  }


  public void setTopMargin(int topMargin) {
    this.topMargin = topMargin;
  }


  public void setBottomMargin(int bottomMargin) {
    this.bottomMargin = bottomMargin;
  }


  public void draw(String text, BufferedImage canvas, FontFactory fontFactory, ColorFactory colorFactory) {
    Graphics2D g = (Graphics2D) canvas.getGraphics();
    TextString ts = convertToCharacters(text, g, fontFactory, colorFactory);
    arrangeCharacters(canvas.getWidth(), canvas.getHeight(), ts);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    for (TextCharacter tc : ts.getCharacters()) {
      g.setColor(tc.getColor());
      g.drawString(tc.iterator(), (float) tc.getX(), (float) tc.getY());
    }
  }

  protected TextString convertToCharacters(String text, Graphics2D g, FontFactory fontFactory, ColorFactory colorFactory) {
    TextString characters = new TextString();
    FontRenderContext frc = g.getFontRenderContext();
    double lastx = 0;
    for (int i = 0; i < text.length(); i++) {
      Font font = fontFactory.getFont(i);
      char c = text.charAt(i);
      FontMetrics fm = g.getFontMetrics(font);
      Rectangle2D bounds = font.getStringBounds(String.valueOf(c), frc);
      TextCharacter tc = new TextCharacter();
      tc.setCharacter(c);
      tc.setFont(font);
      tc.setWidth(fm.charWidth(c));
      tc.setHeight(fm.getAscent() + fm.getDescent());
      tc.setAscent(fm.getAscent());
      tc.setDescent(fm.getDescent());
      tc.setX(lastx);
      tc.setY(0);
      tc.setFont(font);
      tc.setColor(colorFactory.getColor(i));
      lastx += bounds.getWidth();
      characters.addCharacter(tc);
    }
    return characters;
  }

}
