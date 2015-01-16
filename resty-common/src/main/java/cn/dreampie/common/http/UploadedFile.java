package cn.dreampie.common.http;

import java.io.File;

/**
 * Created by ice on 15-1-6.
 */ // A class to hold information about an uploaded file.
//
public class UploadedFile {

  private String dir;
  private String filename;
  private String original;
  private String type;

  public UploadedFile(String dir, String filename, String original, String type) {
    this.dir = dir;
    this.filename = filename;
    this.original = original;
    this.type = type;
  }

  public String getContentType() {
    return type;
  }

  public String getFilesystemName() {
    return filename;
  }

  public String getOriginalFileName() {
    return original;
  }

  public File getFile() {
    if (dir == null || filename == null) {
      return null;
    } else {
      return new File(dir + File.separator + filename);
    }
  }
}
