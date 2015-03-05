package cn.dreampie.common;

import cn.dreampie.orm.cache.CacheManager;

import java.io.File;

/**
 * Created by ice on 14-12-29.
 */
public final class Constant {
  public static String encoding = "UTF-8";
  public static boolean devMode = false;
  public static boolean cacheEnabled = false;
  public static String uploadDirectory = File.separator + "upload" + File.separator;
  public static int uploadMaxSize = 1024 * 1024 * 10;// 10 Meg
  public static String[] uploadDenieds = new String[]{};
  public static CacheManager cacheManager;
  public static boolean showRoute = false;
  public static String apiPrefix = "/api";
}
