package cn.dreampie.route.holder;

import cn.dreampie.common.Constant;
import cn.dreampie.common.Render;
import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.common.http.exception.WebException;
import cn.dreampie.common.http.result.HttpStatus;
import cn.dreampie.log.Logger;
import cn.dreampie.route.render.RenderFactory;

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
          logger.warn("Request \"" + request.getHttpMethod() + " " + request.getRestPath() + "\" error : " + webException.getMessage());
        }
        response.setStatus(webException.getStatus());
        render.render(request, response, webException.getContent());
      } else {
        //其他访问  跳转到 指定页面
        go(response, webException.getStatus(), isHandled);
      }
    } else {
      if (logger.isErrorEnabled()) {
        logger.warn("Request \"" + request.getHttpMethod() + " " + request.getRestPath() + "\" error : " + exception.getMessage(), exception);
      }
      response.setStatus(HttpStatus.BAD_REQUEST);
      render.render(request, response, exception.getMessage());
    }
  }
}
