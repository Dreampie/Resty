package cn.dreampie.route.holder;

import cn.dreampie.common.Constant;
import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.common.http.result.HttpStatus;
import cn.dreampie.log.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dreampie on 15/4/27.
 */
public abstract class ExceptionHolder {
  private final static Logger logger = Logger.getLogger(ExceptionHolder.class);

  public final static ExceptionHolder HOLDER;
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

  public static void addExceptionHold(HttpStatus status, String url) {
    addExceptionHold(status, url, false);
  }

  public static void addExceptionHold(HttpStatus status, String url, boolean redirect) {
    if (redirect) {
      redirectMap.put(status, url);
    } else {
      forwardMap.put(status, url);
    }
  }

  public static String getForward(HttpStatus status) {
    return forwardMap.get(status);
  }

  public static String getRedirect(HttpStatus status) {
    return redirectMap.get(status);
  }

  public abstract void hold(HttpRequest request, HttpResponse response, Exception exception, boolean[] isHandled);
}
