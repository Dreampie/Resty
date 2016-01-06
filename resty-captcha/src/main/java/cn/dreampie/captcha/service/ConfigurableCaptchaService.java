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

import cn.dreampie.captcha.background.SingleColorBackgroundFactory;
import cn.dreampie.captcha.color.SingleColorFactory;
import cn.dreampie.captcha.filter.predefined.CurvesRippleFilterFactory;
import cn.dreampie.captcha.font.RandomFontFactory;
import cn.dreampie.captcha.text.renderer.BestFitTextRenderer;
import cn.dreampie.captcha.word.AdaptiveRandomWordFactory;

public class ConfigurableCaptchaService extends AbstractCaptchaService {

  public ConfigurableCaptchaService() {
    backgroundFactory = new SingleColorBackgroundFactory();
    wordFactory = new AdaptiveRandomWordFactory();
    fontFactory = new RandomFontFactory();
    textRenderer = new BestFitTextRenderer();
    colorFactory = new SingleColorFactory();
    filterFactory = new CurvesRippleFilterFactory(colorFactory);
    textRenderer.setLeftMargin(10);
    textRenderer.setRightMargin(10);
    width = 160;
    height = 70;
  }

}
