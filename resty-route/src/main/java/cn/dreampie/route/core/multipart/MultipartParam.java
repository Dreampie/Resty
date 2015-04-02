package cn.dreampie.route.core.multipart;

import cn.dreampie.common.http.UploadedFile;

import java.util.Hashtable;
import java.util.List;

/**
 * Created by wangrenhui on 15/4/2.
 */
public class MultipartParam {
  private Hashtable<String, UploadedFile> uploadedFiles;
  private Hashtable<String, List<String>> parameters;  // name - Vector of values

  public MultipartParam(Hashtable<String, UploadedFile> uploadedFiles, Hashtable<String, List<String>> parameters) {
    this.uploadedFiles = uploadedFiles;
    this.parameters = parameters;
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

  public Hashtable<String, List<String>> getParameters() {
    return parameters;
  }

  public List<String> getParameter(String name) {
    if (parameters != null) {
      return parameters.get(name);
    }
    return null;
  }

  public String getParameterFirst(String name) {
    if (parameters != null) {
      List<String> value = parameters.get(name);
      return value != null && value.size() > 0 ? value.get(0) : null;
    }
    return null;
  }
}
