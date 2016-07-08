package cn.dreampie.client;

import cn.dreampie.common.util.HttpTyper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static cn.dreampie.common.util.Checker.checkNotNull;

/**
 * Created by Dreampie on 16/1/5.
 */
public class HttpClientFile {
  private String name;
  private String contentType;
  private InputStream inputStream;

  public HttpClientFile(String filepath) throws FileNotFoundException {
    this(new File(filepath));
  }

  public HttpClientFile(File file) throws FileNotFoundException {
    this.name = file.getName();
    this.contentType = HttpTyper.getContentTypeFromFileName(name);
    this.inputStream = new FileInputStream(file);
  }

  public HttpClientFile(String name, String contentType, InputStream inputStream) {
    this.name = name;
    this.contentType = contentType;
    this.inputStream = checkNotNull(inputStream);
  }

  public String getName() {
    return name;
  }

  public String getContentType() {
    return contentType;
  }

  public InputStream getInputStream() {
    return inputStream;
  }
}
