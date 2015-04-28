package cn.dreampie.common.util.stream;

import cn.dreampie.common.Constant;
import cn.dreampie.log.Logger;

import java.io.File;
import java.io.IOException;

/**
 * An interface to provide a pluggable file renaming policy, particularly
 * useful to handle naming conflicts with an existing file.
 *
 * @author Jason Hunter
 * @version 1.0, 2002/04/30, initial revision, thanks to Changshin Lee for
 *          the basic idea
 */
public abstract class FileRenamer {
  public final static FileRenamer RENAMER;
  private final static Logger logger = Logger.getLogger(FileRenamer.class);

  static {
    FileRenamer fileRenamer = null;
    if (Constant.fileRenamer == null) {
      fileRenamer = new DefaultFileRenamer();
    } else {
      try {
        Class renameClass = Class.forName(Constant.fileRenamer);
        fileRenamer = (FileRenamer) renameClass.newInstance();
      } catch (ClassNotFoundException e) {
        logger.error("Could not found FileRenamer Class.", e);
      } catch (InstantiationException e) {
        logger.error("Could not init FileRenamer Class.", e);
      } catch (IllegalAccessException e) {
        logger.error("Could not access FileRenamer Class.", e);
      }
    }
    RENAMER = fileRenamer;
  }

  /**
   * Returns a File object holding a new name for the specified file.
   */
  public abstract File rename(File f);


  protected boolean createNewFile(File f) {
    try {
      return f.createNewFile();
    } catch (IOException ignored) {
      return false;
    }
  }
}
