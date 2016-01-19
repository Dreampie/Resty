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


public abstract class AbstractTransformImageOp extends AbstractImageOp {

  private boolean initialized;

  public AbstractTransformImageOp() {
    setEdgeMode(EDGE_CLAMP);
  }

  protected abstract void transform(int x, int y, double[] t);

  protected void init() {
  }


  protected void filter(int[] inPixels, int[] outPixels, int width, int height) {
    if (!initialized) {
      init();
      initialized = true;
    }
    double[] t = new double[2];
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        transform(x, y, t);
        int pixel = getPixelBilinear(inPixels, t[0], t[1], width, height, getEdgeMode());
        outPixels[x + y * width] = pixel;
      }
    }
  }

}

