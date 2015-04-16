package cn.dreampie.route.core.multipart;

import cn.dreampie.common.http.UploadedFile;

import java.util.Hashtable;
import java.util.List;

/**
 * Created by wangrenhui on 15/4/2.
 */
public class MultipartParam {
  private Hashtable<String, UploadedFile> uploadedFiles;
  private Hashtable<String, List<String>> params;  // name - Vector of values

  public MultipartParam(Hashtable<String, UploadedFile> uploadedFiles, Hashtable<String, List<String>> params) {
    this.uploadedFiles = uploadedFiles;
    this.params = params;
  }

  public Hashtable<String, UploadedFile> getUploadedFiles() {
    return uploadedFiles;
  }

  public UploadedFile getUploadedFileFirst() {
    return uploadedFiles != null && uploadedFiles.size() > 0 ? uploadedFiles.values().iterator().next() : null;
  }

  public UploadedFile getUploadedFile(String filename) {
    if (uploadedFiles != null) {
      return uploadedFiles.get(filename);
    }
    return null;
  }

  public Hashtable<String, List<String>> getParams() {
    return params;
  }

  public List<String> getParam(String name) {
    if (params != null) {
      return params.get(name);
    }
    return null;
  }

  public String getParamFirst(String name) {
    if (params != null) {
      List<String> value = params.get(name);
      return value != null && value.size() > 0 ? value.get(0) : null;
    }
    return null;
  }
}
