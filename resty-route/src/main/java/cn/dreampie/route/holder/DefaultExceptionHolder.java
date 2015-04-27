package cn.dreampie.route.holder;

import cn.dreampie.common.Constant;
import cn.dreampie.common.Render;
import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.common.http.exception.WebException;
import cn.dreampie.common.http.result.HttpStatus;
import cn.dreampie.log.Logger;
import cn.dreampie.route.render.RenderFactory;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Created by Dreampie on 15/4/27.
 */
public class DefaultExceptionHolder extends ExceptionHolder {
  private final static Logger logger = Logger.getLogger(DefaultExceptionHolder.class);

  public void hold(HttpRequest request, HttpResponse response, Exception exception, boolean[] isHandled) {
    String restPath = request.getRestPath();
    Render render = RenderFactory.getByUrl(restPath);
    if (exception instanceof WebException) {
      WebException webException = (WebException) exception;
      //api访问 所有的异常 以httpStatus返回
      if (Constant.apiPrefix == null || restPath.startsWith(Constant.apiPrefix)) {
        if (logger.isWarnEnabled()) {
          logger.warn("Request \"" + request.getHttpMethod() + " " + request.getRestPath() + "\" error - " + webException.getMessage());
        }
        response.setStatus(webException.getStatus());
        render.render(request, response, webException.getContent());
      } else {
        String url = getForward(webException.getStatus());
        //其他访问  跳转到 指定页面
        try {
          if (url != null) {
            response.forward(url);
          } else {
            url = getRedirect(webException.getStatus());
            if (url != null) {
              response.sendRedirect(url);
            } else {
              isHandled[0] = false;
            }
          }
        } catch (ServletException e) {
          logger.error("Request show '" + url + "' error.", e);
        } catch (IOException e) {
          logger.error("Request show '" + url + "' error.", e);
        }
      }
    } else {
      response.setStatus(HttpStatus.BAD_REQUEST);
      render.render(request, response, exception.getMessage());
      if (logger.isErrorEnabled()) {
        logger.error(request.getRestPath(), exception);
      }
    }
  }
}
