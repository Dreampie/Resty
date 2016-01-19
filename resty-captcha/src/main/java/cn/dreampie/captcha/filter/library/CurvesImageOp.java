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
package cn.dreampie.captcha.filter.library;

import java.awt.image.BufferedImage;


public class CurvesImageOp extends AbstractImageOp {

  private float strokeMin;
  private float strokeMax;

  public float getStrokeMin() {
    return strokeMin;
  }

  public void setStrokeMin(float strokeMin) {
    this.strokeMin = strokeMin;
  }

  public float getStrokeMax() {
    return strokeMax;
  }

  public void setStrokeMax(float strokeMax) {
    this.strokeMax = strokeMax;
  }

  public BufferedImage filter(BufferedImage src, BufferedImage dest) {
    return src;
  }
}
