package cn.dreampie.common.util.stream;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

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

  public static List<File> files(String dir) {
    return files(new File(dir));
  }

  public static List<File> files(File dir) {
    List<File> result = new ArrayList<File>();
    if (dir.exists()) {
      File[] files = dir.listFiles();
      if (files != null && files.length > 0) {
        for (File file : files) {
          if (!file.isHidden()) {
            if (file.isDirectory()) {
              result.addAll(files(new File(file.getAbsolutePath())));
            } else {
              result.add(file);
            }
          }
        }
      }
    }
    return result;
  }
}
