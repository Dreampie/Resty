package cn.dreampie.common.util.stream;

import java.io.File;

/**
 * Created by ice on 15-1-28.
 */
public class Filer {

  public static File mkDirs(String path) {
    File file = new File(path);
    File parent = file.getParentFile();
    if (!parent.exists()) {
      if (!parent.mkdirs()) {
        throw new FileException("Directory " + parent.getAbsolutePath() + " not exists and can not create directory.");
      }
    }
    return file;
  }
}
