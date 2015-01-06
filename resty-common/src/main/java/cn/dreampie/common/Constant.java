package cn.dreampie.common;

import cn.dreampie.orm.cache.CacheManager;
import cn.dreampie.orm.cache.EHCacheManager;

import java.io.File;

/**
 * Created by ice on 14-12-29.
 */
public final class Constant {
  public static String encoding = "UTF-8";
  public static boolean dev_mode = false;
  public static boolean cache_enabled = false;
  public static CacheManager cacheManager = new EHCacheManager();
  public static String uploadDirectory = File.separator + "upload" + File.separator;
  public static int uploadMaxSize = 1024 * 1024 * 10;// 10 Meg
  public static String[] uploadDenieds = new String[]{};
}
