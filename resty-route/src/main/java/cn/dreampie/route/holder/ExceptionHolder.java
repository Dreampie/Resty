package cn.dreampie.route.holder;

import cn.dreampie.common.Constant;
import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.common.http.result.HttpStatus;
import cn.dreampie.log.Logger;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static cn.dreampie.common.util.Checker.checkNotNull;

/**
 * Created by Dreampie on 15/4/27.
 */
public abstract class ExceptionHolder {
  public final static ExceptionHolder HOLDER;
  private final static Logger logger = Logger.getLogger(ExceptionHolder.class);
  private static String defaultUrl;
  private static boolean forward;
  private static Map<HttpStatus, String> forwardMap = new HashMap<HttpStatus, String>();
  private static Map<HttpStatus, String> redirectMap = new HashMap<HttpStatus, String>();

  static {
    ExceptionHolder exceptionHolder = null;
    if (Constant.exceptionHolder == null) {
      exceptionHolder = new DefaultExceptionHolder();
    } else {
      try {
        Class holderClass = Class.forName(Constant.exceptionHolder);
        exceptionHolder = (ExceptionHolder) holderClass.newInstance();
      } catch (ClassNotFoundException e) {
        logger.error("Could not found ExceptionHolder Class.", e);
      } catch (InstantiationException e) {
        logger.error("Could not init ExceptionHolder Class.", e);
      } catch (IllegalAccessException e) {
        logger.error("Could not access ExceptionHolder Class.", e);
      }
    }
    HOLDER = exceptionHolder;
  }

  public static void setDefaultForward(String url) {
    setDefault(url, true);
  }

  public static void setDefaultRedirect(String url) {
    setDefault(url, false);
  }

  private static void setDefault(String url, boolean isForward) {
    if (defaultUrl != null) {
      throw new IllegalArgumentException("Default url only can set once.");
    }
    defaultUrl = checkNotNull(url, "Url could not be null.");
    forward = isForward;
  }

  public static void addFoward(HttpStatus status, String url) {
    forwardMap.put(status, checkNotNull(url, "Url could not be null."));
  }

  public static void addRedirect(HttpStatus status, String url) {
    redirectMap.put(status, checkNotNull(url, "Url could not be null."));
  }

  /**
   * 捕获异常 并就行跳转
   *
   * @param response  response
   * @param status    status
   * @param isHandled isHandled
   * @return url
   * @throws ServletException
   * @throws IOException
   */
  protected static void go(HttpResponse response, HttpStatus status, boolean[] isHandled) {
    String url = forwardMap.get(status);
    try {
      //forwar
      if (url != null) {
        response.forward(url);
      } else {
        url = redirectMap.get(status);
        //redirect
        if (url != null) {
          response.sendRedirect(url);
        } else {
          //默认跳转
          if (defaultUrl != null) {
            url = defaultUrl;
            if (forward) {
              response.forward(defaultUrl);
            } else {
              response.sendRedirect(defaultUrl);
            }
          } else {
            isHandled[0] = false;
            if (logger.isWarnEnabled()) {
              logger.warn("Resty not handle this request.");
            }
          }
        }
      }
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Response going error.", e);
      }
    }
  }

  /**
   * 处理异常
   *
   * @param request
   * @param response
   * @param exception
   * @param isHandled
   */
  public abstract void hold(HttpRequest request, HttpResponse response, Exception exception, boolean[] isHandled);
}
