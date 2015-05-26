package cn.dreampie.resource;

import cn.dreampie.common.util.stream.FileRenamer;

import java.io.File;

/**
 * @author Dreampie
 * @date 2015-05-23
 * @what
 */
public class MyFileRenamer extends FileRenamer {
  public File rename(File f) {
    // change dir
    return new File(f.getAbsolutePath().replace("upload", "xx"));
  }
}
