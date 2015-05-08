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

package cn.dreampie.route.interceptor.exception;

/**
 * InterceptorException
 */
public class InterceptorException extends RuntimeException {

  public InterceptorException() {
  }

  public InterceptorException(String message) {
    super(message);
  }

  public InterceptorException(Throwable cause) {
    super(cause);
  }

  public InterceptorException(String message, Throwable cause) {
    super(message, cause);
  }
}










