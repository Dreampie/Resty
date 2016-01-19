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
package cn.dreampie.captcha.font;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class RandomFontFactory implements FontFactory {

  protected List<String> families;
  protected int minSize;
  protected int maxSize;
  protected boolean randomStyle;

  public RandomFontFactory() {
    this(new String[]{"Verdana", "Tahoma"}, 45, false);
  }

  public RandomFontFactory(String[] families, int size, boolean randomStyle) {
    this(families, size, size, randomStyle);
  }

  public RandomFontFactory(String[] families, int minSize, int maxSize, boolean randomStyle) {
    this.families = Arrays.asList(families);
    this.minSize = minSize;
    this.maxSize = maxSize;
    this.randomStyle = randomStyle;
  }

  public RandomFontFactory(List<String> families) {
    this();
    this.families = families;
  }

  public RandomFontFactory(String[] families) {
    this();
    this.families = Arrays.asList(families);
  }

  public RandomFontFactory(int size, List<String> families) {
    this(families);
    minSize = maxSize = size;
  }

  public RandomFontFactory(int size, String[] families) {
    this(families);
    minSize = maxSize = size;
  }

  public void setFamilies(List<String> families) {
    this.families = families;
  }

  public void setMinSize(int minSize) {
    this.minSize = minSize;
  }

  public void setMaxSize(int maxSize) {
    this.maxSize = maxSize;
  }

  public void setRandomStyle(boolean randomStyle) {
    this.randomStyle = randomStyle;
  }


  public Font getFont(int index) {
    Random r = new Random();
    String family = families.get(r.nextInt(families.size()));
    boolean bold = r.nextBoolean() && randomStyle;
    int size = minSize;
    if (maxSize - minSize > 0) {
      size += r.nextInt(maxSize - minSize);
    }
    return new Font(family, bold ? Font.BOLD : Font.PLAIN, size);
  }

}
