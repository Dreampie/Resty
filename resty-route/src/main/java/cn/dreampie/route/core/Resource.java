package cn.dreampie.route.core;


import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.upload.UploadedFile;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Resource
 */
public abstract class Resource {

  private RouteMatch routeMatch;
  private MultipartBuilder multipartBuilder;

  void setRouteMatch(RouteMatch routeMatch) {
    this.routeMatch = routeMatch;
  }

  public String getPath() {
    return routeMatch.getPath();
  }

  public String getPathParam(String paramName) {
    return routeMatch.getPathParam(paramName);
  }

  public Map<String, String> getPathParams() {
    return routeMatch.getPathParams();
  }

  public Map<String, List<String>> getOtherParams() {
    return routeMatch.getOtherParams();
  }

  public List<String> getOtherParam(String name) {
    return routeMatch.getOtherParams().get(name);
  }

  public HttpRequest getRequest() {
    return routeMatch.getRequest();
  }

  public HttpResponse getResponse() {
    return routeMatch.getResponse();
  }

  public void setMultipartBuilder(MultipartBuilder multipartBuilder) {
    this.multipartBuilder = multipartBuilder;
  }

  public Hashtable<String, UploadedFile> getFiles(String saveDirectory, int maxPostSize, String encoding) {
    if (multipartBuilder == null) {
      multipartBuilder = new MultipartBuilder(getRequest(), saveDirectory, maxPostSize, encoding);
    }
    return multipartBuilder.getFiles();
  }

  public Hashtable<String, UploadedFile> getFiles(String saveDirectory, int maxPostSize) {
    return getFiles(saveDirectory, maxPostSize, null);
  }

  public Hashtable<String, UploadedFile> getFiles(String saveDirectory, String encoding) {
    return getFiles(saveDirectory, -1, encoding);
  }

  public Hashtable<String, UploadedFile> getFiles(String saveDirectory) {
    return getFiles(saveDirectory, null);
  }

  public Hashtable<String, UploadedFile> getFiles() {
    return getFiles(null);
  }

  public UploadedFile getFile() {
    Hashtable<String, UploadedFile> uploadFiles = getFiles();
    return uploadFiles.size() > 0 ? uploadFiles.values().iterator().next() : null;
  }

  public UploadedFile getFile(String filename) {
    Hashtable<String, UploadedFile> uploadFiles = getFiles();
    return uploadFiles.get(filename);
  }

  public Hashtable<String, List<String>> getParameters() {
    if (multipartBuilder == null)
      getFiles();//默认的上传文件
    return multipartBuilder.getParameters();
  }

  public List<String> getParameters(String param) {
    Hashtable<String, List<String>> parameters = getParameters();
    return parameters.get(param);
  }

}


