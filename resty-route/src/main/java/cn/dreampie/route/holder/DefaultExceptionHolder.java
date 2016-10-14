package cn.dreampie.route.holder;

import cn.dreampie.common.Constant;
import cn.dreampie.common.Render;
import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.common.http.exception.HttpException;
import cn.dreampie.common.http.result.HttpStatus;
import cn.dreampie.common.util.json.Jsoner;
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
    String message;
    if (exception instanceof HttpException) {
      HttpException httpException = (HttpException) exception;
      //api访问 所有的异常 以httpStatus返回
      if (Constant.apiPrefix == null || restPath.startsWith(Constant.apiPrefix)) {
        message = Jsoner.toJSON(httpException.getContent());
        if (logger.isWarnEnabled()) {
          logger.warn("Request \"" + request.getHttpMethod() + " " + request.getRestPath() + "\" error : " + httpException.getStatus().getCode() + " " + message);
        }
        response.setStatus(httpException.getStatus());
        render.render(request, response, message);
      } else {
        //其他访问  跳转到 指定页面
        go(response, httpException.getStatus(), isHandled);
      }
    } else {
      message = exception.getMessage();
      if (message == null) {
        Throwable throwable = exception.getCause();
        if (throwable != null) {
          message = throwable.getMessage();
        }
      }
      if (logger.isErrorEnabled()) {
        logger.warn("Request \"" + request.getHttpMethod() + " " + request.getRestPath() + "\" error : " + HttpStatus.BAD_REQUEST.getCode() + " " + message, exception);
      }
      response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
      render.render(request, response, message);
    }
  }
}
