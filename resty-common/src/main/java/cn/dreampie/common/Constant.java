package cn.dreampie.common;

import cn.dreampie.common.util.properties.Prop;
import cn.dreampie.common.util.properties.Proper;
import cn.dreampie.log.Logger;
import cn.dreampie.orm.cache.CacheManager;
import cn.dreampie.orm.cache.EHCacheManager;

import java.io.File;

/**
 * Created by ice on 14-12-29.
 */
public final class Constant {

  private final static Logger logger = Logger.getLogger(Constant.class);

  public final static String encoding;
  public final static boolean devMode;
  public final static boolean cacheEnabled;
  public final static String uploadDirectory;
  public final static Integer uploadMaxSize;// 10 Meg
  public final static String[] uploadDenieds;//set file content type eg. text/xml
  public final static CacheManager cacheManager;// 缓存工具
  public final static boolean showRoute;
  public final static String apiPrefix;

  static {
    Prop constants = null;
    try {
      constants = Proper.use("application.properties");
    } catch (Exception e) {
      logger.warn(e.getMessage());
    }
    if (constants == null) {
      encoding = "UTF-8";
      devMode = false;
      cacheEnabled = false;
      uploadDirectory = File.separator + "upload" + File.separator;
      uploadMaxSize = 1024 * 1024 * 10;
      uploadDenieds = new String[]{};
      showRoute = false;
      apiPrefix = "/api";
      cacheManager = null;
    } else {
      encoding = constants.get("app.encoding", "UTF-8");
      devMode = constants.getBoolean("app.devMode", false);
      cacheEnabled = constants.getBoolean("app.cacheEnabled", false);
      uploadDirectory = constants.get("app.uploadDirectory", File.separator + "upload" + File.separator);
      uploadMaxSize = constants.getInt("app.uploadMaxSize", 1024 * 1024 * 10);
      String uploadDeniedStr = constants.get("app.uploadDenieds");
      if (uploadDeniedStr == null)
        uploadDenieds = new String[]{};
      else
        uploadDenieds = uploadDeniedStr.split(",");
      showRoute = constants.getBoolean("app.showRoute", false);
      apiPrefix = constants.get("app.showRoute", "/api");

      CacheManager cm = null;
      if (cacheEnabled) {
        String cacheManagerClassStr = constants.get("app.cacheManager");
        if (cacheManagerClassStr == null) {
          cm = new EHCacheManager();
        } else {
          try {
            Class cacheClass = Class.forName(cacheManagerClassStr);
            cm = (CacheManager) cacheClass.newInstance();
          } catch (ClassNotFoundException e) {
            logger.error("Could not found CacheManager Class.", e);
          } catch (InstantiationException e) {
            logger.error("Could not init CacheManager Class.", e);
          } catch (IllegalAccessException e) {
            logger.error("Could not access CacheManager Class.", e);
          }
        }
      }
      cacheManager = cm;
    }
  }
}
