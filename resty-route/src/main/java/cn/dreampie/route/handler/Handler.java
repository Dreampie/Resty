/**
 * Copyright (c) 2011-2015, James Zhan 詹波 (jfinal@126.com).
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.dreampie.route.handler;

import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;

/**
 * Handler.
 * You can config Handler in JFinalConfig.configHandler() method,
 * Handler can do anything under the jfinal action.
 */
public abstract class Handler {

  protected Handler nextHandler;

  /**
   * Handle target
   *
   * @param request   HttpServletRequest of this http request
   * @param response  HttpServletRequest of this http request
   * @param isHandled RestjFilter will invoke doFilter() method if isHandled[0] == false,
   *                  it is usually to tell Filter should handle the static resource.
   */
  public abstract void handle(HttpRequest request, HttpResponse response, boolean[] isHandled);
}




