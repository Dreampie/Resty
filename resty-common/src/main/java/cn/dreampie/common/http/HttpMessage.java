package cn.dreampie.common.http;

import cn.dreampie.common.Constant;
import cn.dreampie.common.http.result.HttpStatus;
import cn.dreampie.common.util.properties.Prop;
import cn.dreampie.common.util.properties.Proper;
import cn.dreampie.log.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Dreampie on 2016/11/9.
 */
public class HttpMessage {

  private final static Logger logger = Logger.getLogger(Constant.class);

  public static final String OK = "ok";
  public static final String CREATED = "created";
  public static final String ACCEPTED = "accepted";
  public static final String NOT_MODIFIED = "notModified";
  public static final String BAD_REQUEST = "badRequest";
  public static final String UNAUTHORIZED = "unauthorized";
  public static final String FORBIDDEN = "forbidden";
  public static final String NOT_FOUND = "notFound";
  public static final String API_NOT_FOUND = "apiNotFound";
  public static final String NOT_ACCEPTABLE = "notAcceptable";
  public static final String METHOD_NOT_ALLOWED = "methodNotAllowed";
  public static final String UNSUPPORTED_MEDIA_TYPE = "unsupportedMediaType";
  public static final String UNPROCESSABLE_ENTITY = "unprocessableEntity";
  public static final String GONE = "gone";
  public static final String INTERNAL_SERVER_ERROR = "internalServerError";
  public static final String SERVICE_UNAVAILABLE = "serviceUnavailable";
  public static final String TOO_MANY_REQUESTS = "tooManyRequests";
  public static final String REQUEST_ENTITY_TOO_LARGE = "requestEntityTooLarge";

  //logic error
  public static final String HEADER_NOT_MATCH = "headerNotMatch";
  public static final String FILE_UPLOAD_ERROR = "fileUploadError";
  public static final String FILE_DOWNLOAD_ERROR = "fileDownloadError";
  public static final String CORS_FAILED = "CORSFailed";
  public static final String USERNAME_NOT_FOUND = "usernameNotFound";
  public static final String PASSWORD_ERROR = "passwordError";

  private static final Map<String, String> messages = new HashMap<String, String>();
  private static final Map<String, HttpStatus> status = new HashMap<String, HttpStatus>() {{
    put(OK, HttpStatus.OK);
    put(CREATED, HttpStatus.CREATED);
    put(ACCEPTED, HttpStatus.ACCEPTED);
    put(NOT_MODIFIED, HttpStatus.NOT_MODIFIED);
    put(BAD_REQUEST, HttpStatus.BAD_REQUEST);
    put(UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
    put(FORBIDDEN, HttpStatus.FORBIDDEN);
    put(NOT_FOUND, HttpStatus.NOT_FOUND);
    put(API_NOT_FOUND, HttpStatus.NOT_FOUND);
    put(NOT_ACCEPTABLE, HttpStatus.NOT_ACCEPTABLE);
    put(METHOD_NOT_ALLOWED, HttpStatus.METHOD_NOT_ALLOWED);
    put(UNSUPPORTED_MEDIA_TYPE, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    put(UNPROCESSABLE_ENTITY, HttpStatus.UNPROCESSABLE_ENTITY);
    put(GONE, HttpStatus.GONE);
    put(INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    put(SERVICE_UNAVAILABLE, HttpStatus.SERVICE_UNAVAILABLE);
    put(TOO_MANY_REQUESTS, HttpStatus.TOO_MANY_REQUESTS);
    put(REQUEST_ENTITY_TOO_LARGE, HttpStatus.REQUEST_ENTITY_TOO_LARGE);

    //logic error
    put(HEADER_NOT_MATCH, HttpStatus.NOT_ACCEPTABLE);
    put(FILE_UPLOAD_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    put(FILE_DOWNLOAD_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    put(CORS_FAILED, HttpStatus.FORBIDDEN);
    put(USERNAME_NOT_FOUND, HttpStatus.UNPROCESSABLE_ENTITY);
    put(PASSWORD_ERROR, HttpStatus.UNPROCESSABLE_ENTITY);
  }};

  static {
    Prop constants = null;
    try {
      constants = Proper.use("application.properties");
    } catch (Exception e) {
      logger.warn(e.getMessage());
    }

    messages.put(OK, "执行成功.");
    messages.put(CREATED, "创建成功.");
    messages.put(ACCEPTED, "接收并异步处理.");
    messages.put(NOT_MODIFIED, "没有更新.");
    messages.put(BAD_REQUEST, "客户端请求错误.");
    messages.put(UNAUTHORIZED, "未登录.");
    messages.put(FORBIDDEN, "拒绝访问.");
    messages.put(NOT_FOUND, "访问资源不存在.");
    messages.put(API_NOT_FOUND, "访问接口不存在.");
    messages.put(NOT_ACCEPTABLE, "不能接受的请求.");
    messages.put(METHOD_NOT_ALLOWED, "请求方法不支持.");
    messages.put(UNSUPPORTED_MEDIA_TYPE, "请求数据类型不支持.");
    messages.put(UNPROCESSABLE_ENTITY, "请求参数错误.");
    messages.put(GONE, "接口已经失效.");
    messages.put(INTERNAL_SERVER_ERROR, "服务器错误.");
    messages.put(SERVICE_UNAVAILABLE, "服务不可访问.");
    messages.put(TOO_MANY_REQUESTS, "请求次数超过限制.");
    messages.put(REQUEST_ENTITY_TOO_LARGE, "请求内容超过大小限制.");

    //logic error
    messages.put(HEADER_NOT_MATCH, "不支持该请求的头信息.");
    messages.put(FILE_UPLOAD_ERROR, "文件上传失败.");
    messages.put(FILE_DOWNLOAD_ERROR, "文件下载失败.");
    messages.put(CORS_FAILED, "跨域请求失败.");
    messages.put(USERNAME_NOT_FOUND, "用户名不存在.");
    messages.put(PASSWORD_ERROR, "密码错误.");
    if (constants != null) {
      Set<String> keys = constants.getKeys();
      for (String key : keys) {
        if (key.startsWith("http.")) {
          String useKey = key.split("\\.")[2];
          if (key.startsWith("http.status.")) {
            HttpStatus oldStatus = status.get(useKey);
            if (oldStatus != null) {
              status.put(useKey, HttpStatus.havingCode(constants.getInt(key, oldStatus.getCode())));
            } else {
              status.put(useKey, HttpStatus.havingCode(constants.getInt(key)));
            }
          }

          if (key.startsWith("http.messages.")) {
            messages.put(useKey, constants.get(key, messages.get(key)));
          }
        }
      }
    }
  }


  public static String getMessage(String key) {
    return messages.get(key);
  }

  public static HttpStatus getStatus(String key) {
    return status.get(key);
  }
}
