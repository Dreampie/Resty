package cn.dreampie.client;

import cn.dreampie.common.util.Checker;
import cn.dreampie.common.util.HttpTyper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by Dreampie on 16/1/5.
 */
public class ClientFile {
  private String name;
  private String mimeType;
  private InputStream inputStream;

  public ClientFile(String filepath) throws FileNotFoundException {
    this(new File(filepath));
  }

  public ClientFile(File file) throws FileNotFoundException {
    this.name = file.getName();
    this.mimeType = HttpTyper.getContentTypeFromExtension(name);
    this.inputStream = new FileInputStream(file);
  }

  public ClientFile(String name, String mimeType, InputStream inputStream) {
    this.name = name;
    this.mimeType = mimeType;
    this.inputStream = Checker.checkNotNull(inputStream);
  }

  public String getName() {
    return name;
  }

  public String getMimeType() {
    return mimeType;
  }

  public InputStream getInputStream() {
    return inputStream;
  }
}
