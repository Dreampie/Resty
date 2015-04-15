package cn.dreampie.common.util.stream;

import java.io.File;

/**
 * An interface to provide a pluggable file renaming policy, particularly
 * useful to handle naming conflicts with an existing file.
 *
 * @author Jason Hunter
 * @version 1.0, 2002/04/30, initial revision, thanks to Changshin Lee for
 *          the basic idea
 */
public interface FileRenamer {

  /**
   * Returns a File object holding a new name for the specified file.
   */
  public File rename(File f);

}
