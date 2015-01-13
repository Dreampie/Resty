package cn.dreampie.common;

import cn.dreampie.orm.cache.CacheManager;

import java.io.File;

/**
 * Created by ice on 14-12-29.
 */
public final class Constant {
  public static String encoding = "UTF-8";
  public static boolean dev_mode = false;
  public static boolean cache_enabled = false;
  public static String uploadDirectory = File.separator + "upload" + File.separator;
  public static int uploadMaxSize = 1024 * 1024 * 10;// 10 Meg
  public static String[] uploadDenieds = new String[]{};
  public static CacheManager cacheManager;


}
