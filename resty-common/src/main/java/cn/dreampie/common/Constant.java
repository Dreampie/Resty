package cn.dreampie.common;

import cn.dreampie.common.http.Encoding;
import cn.dreampie.common.util.properties.Prop;
import cn.dreampie.common.util.properties.Proper;
import cn.dreampie.log.Logger;

import java.io.File;

/**
 * Created by ice on 14-12-29.
 */
public final class Constant {

  public final static String CONNECTOR = "::";
  public final static String encoding;//编码
  public final static boolean devEnable;//是否使用开发模式
  public final static boolean cacheEnabled;//是否开启缓存
  public final static boolean oneParamParse;//单一参数不用传参数名字
  public final static String uploadDirectory;//文件上传默认目录
  public final static Integer uploadMaxSize;//文件上传最大的尺寸 10 Meg
  public final static String[] uploadDenieds;//set file content type eg. text/xml  拒绝上传的文件类型
  public final static String fileRenamer;// 文件上传重命名类
  public final static String cacheProvider;// 缓存类
  public final static boolean showRoute;//请求时打印route匹配信息
  public final static String apiPrefix;//api开发的标志  比如 /api/v1.0/xxx  起始前缀/api为标志 （当api请求没有匹配到route时，会返回404状态）如果是独立域名 可以不配置该项 表示 所以url都是api访问 当非api类型请求是  没有匹配到route时  foward的url （和api请求时的处理  不一致）
  public final static String exceptionHolder;//exception 处理
  public static final String[] xForwardedSupports;//代理跳转

  public static final String oauthSignInUrl;//oauth  登录地址
  public static final String oauthErrorUrl;// oauth 错误地址
  public static final int oauthExpires; //oauth  过期时间


  public static final boolean cookieSecure; //https 才能传输cookie
  public static final boolean cookieHttpOnly; // http 请求传输cookie，js applet无法获取cookie
  public static final String cookieDomain; // 设置域名 .xx.com 祝根域名
  public static final String cookiePath; //设置路径

  private final static Logger logger = Logger.getLogger(Constant.class);

  static {
    Prop constants = null;
    try {
      constants = Proper.use("application.properties");
    } catch (Exception e) {
      logger.warn(e.getMessage());
    }
    if (constants == null) {
      encoding = Encoding.UTF_8.toString();
      devEnable = false;
      cacheEnabled = false;
      oneParamParse = false;
      uploadDirectory = File.separator + "upload" + File.separator;
      uploadMaxSize = 1024 * 1024 * 10;
      uploadDenieds = new String[]{};
      fileRenamer = null;
      cacheProvider = null;
      showRoute = false;
      apiPrefix = null;
      exceptionHolder = null;
      xForwardedSupports = new String[]{"127.0.0.1"};
      oauthSignInUrl = "";
      oauthErrorUrl = "";
      oauthExpires = 0;
      cookieSecure = false;
      cookieHttpOnly = false;
      cookieDomain = null;
      cookiePath = "/";
    } else {
      encoding = constants.get("app.encoding", Encoding.UTF_8.name());
      devEnable = constants.getBoolean("app.devEnable", false);
      cacheEnabled = constants.getBoolean("app.cacheEnabled", false);
      oneParamParse = constants.getBoolean("app.oneParamParse", false);
      uploadDirectory = constants.get("app.uploadDirectory", File.separator + "upload" + File.separator);
      uploadMaxSize = constants.getInt("app.uploadMaxSize", 1024 * 1024 * 10);
      String uploadDeniedStr = constants.get("app.uploadDenieds");
      if (uploadDeniedStr == null) {
        uploadDenieds = new String[]{};
      } else {
        uploadDenieds = uploadDeniedStr.split(",");
      }
      fileRenamer = constants.get("app.fileRenamer");
      cacheProvider = constants.get("app.cacheProvider");
      showRoute = constants.getBoolean("app.showRoute", false);
      apiPrefix = constants.get("app.apiPrefix");
      exceptionHolder = constants.get("app.exceptionHolder");

      String xForwardedSupportsStr = constants.get("app.xForwardedSupports");
      if (xForwardedSupportsStr == null) {
        xForwardedSupports = new String[]{};
      } else {
        xForwardedSupports = xForwardedSupportsStr.split(",");
      }

      oauthSignInUrl = constants.get("app.oauthSignInUrl");
      oauthErrorUrl = constants.get("app.oauthErrorUrl");
      oauthExpires = constants.getInt("app.oauthExpires", 0);

      cookieSecure = constants.getBoolean("app.cookieSecure", false);
      cookieHttpOnly = constants.getBoolean("app.cookieHttpOnly", false);
      cookieDomain = constants.get("app.cookieDomain");
      cookiePath = constants.get("app.cookiePath", "/");
    }
  }
}
