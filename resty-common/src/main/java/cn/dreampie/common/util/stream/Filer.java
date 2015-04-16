package cn.dreampie.common.util.stream;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

/**
 * Created by ice on 15-1-28.
 */
public class Filer {

  public static File mkDirs(String path) {
    return mkDirs(new File(path));
  }

  public static File mkDirs(File file) {
    if (file == null) {
      throw new FileException("File could not be null.");
    }
    File parent = file.getParentFile();
    if (!parent.exists()) {
      if (!parent.mkdirs()) {
        throw new FileException("Directory " + parent.getAbsolutePath() + " not exists and can not create directory.");
      }
    }
    return file;
  }

  public static boolean exist(String file) {
    Enumeration<URL> urls = null;
    try {
      urls = Thread.currentThread().getContextClassLoader().getResources(file);
      while (urls.hasMoreElements()) {
        return true;
      }
    } catch (IOException e) {
      throw new FileException("Could not getResource from file - " + file, e);
    }
    return false;
  }

}
