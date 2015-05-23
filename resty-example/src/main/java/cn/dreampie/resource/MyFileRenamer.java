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
    if (createNewFile(f)) {
      return f;
    }
    String name = f.getName();
    String body = null;
    String ext = null;

    int dot = name.lastIndexOf(".");
    if (dot != -1) {
      body = name.substring(0, dot);
      ext = name.substring(dot); // includes "."
    } else {
      body = name;
      ext = "";
    }


    int count = 0;
    while (!createNewFile(f) && count < 99) {
      count++;
      String newName = body + count + ext;
      f = new File(f.getParent(), newName);
    }

    return f;
  }
}
