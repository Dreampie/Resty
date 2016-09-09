package cn.dreampie.common.util.scan;

import cn.dreampie.common.util.Lister;
import cn.dreampie.log.Logger;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Created by Dreampie on 16/9/7.
 */
public class FileScaner extends Scaner<FileScaner> {

  private static final Logger logger = Logger.getLogger(FileScaner.class);
  private boolean isAbsolutePath = false;


  public FileScaner isAbsolutePath(boolean isAbsolutePath) {
    this.isAbsolutePath = isAbsolutePath;
    return this;
  }

  /**
   * 要扫描的类父级
   *
   * @return scaner
   */
  public static FileScaner of() {
    return new FileScaner().scanInJar(false).targetPattern("*.*");
  }

  /**
   * 扫描文件
   *
   * @param baseDir
   * @return
   */
  protected Enumeration<URL> urlSolve(String baseDir) {
    if (isAbsolutePath) {
      try {
        if (!baseDir.contains("/") && baseDir.contains(".")) {
          baseDir = baseDir.replaceAll("\\.", "/");
        }
        File file = new File(baseDir);
        return Collections.enumeration(Lister.<URL>of(file.toURI().toURL()));
      } catch (MalformedURLException e) {
        logger.error(e.getMessage(), e);
      }
    } else {
      super.urlSolve(baseDir);
    }
    return null;
  }
}
