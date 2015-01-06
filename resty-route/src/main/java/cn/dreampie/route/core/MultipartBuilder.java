package cn.dreampie.route.core;

import cn.dreampie.common.Constant;
import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.exception.WebException;
import cn.dreampie.upload.MultipartRequest;
import cn.dreampie.upload.UploadedFile;
import cn.dreampie.upload.multipart.DefaultFileRenamePolicy;
import cn.dreampie.upload.multipart.FileRenamePolicy;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

/**
 * Created by ice on 15-1-6.
 */
public class MultipartBuilder {
  private String saveDirectory = Constant.uploadDirectory;
  private int maxPostSize = Constant.uploadMaxSize;
  private String encoding = Constant.encoding;
  private FileRenamePolicy fileRenamePolicy = new DefaultFileRenamePolicy();
  private HttpRequest request;
  private MultipartRequest multipartRequest;
  private Hashtable<String, UploadedFile> uploadedFiles = new Hashtable<String, UploadedFile>();

  public MultipartBuilder(HttpRequest request, String saveDirectory, int maxPostSize, String encoding, FileRenamePolicy fileRenamePolicy) {
    if (request == null)
      throw new IllegalArgumentException("Could not found httpRequest for multipartRequest.");
    this.request = request;

    if (saveDirectory != null) {
      if (saveDirectory.startsWith("/"))
        this.saveDirectory = saveDirectory;
      else
        this.saveDirectory = saveDirectory;
    }
    if (maxPostSize > 0)
      this.maxPostSize = maxPostSize;
    if (encoding != null)
      this.encoding = encoding;
    if (fileRenamePolicy != null)
      this.fileRenamePolicy = fileRenamePolicy;
  }

  public MultipartBuilder(HttpRequest request, String saveDirectory, int maxPostSize, String encoding) {
    this(request, saveDirectory, maxPostSize, encoding, null);
  }

  public MultipartBuilder(HttpRequest request, String saveDirectory, int maxPostSize) {
    this(request, saveDirectory, maxPostSize, null, null);
  }

  public MultipartBuilder(HttpRequest request, String saveDirectory, String encoding) {
    this(request, saveDirectory, -1, encoding, null);
  }

  public MultipartBuilder(HttpRequest request, String saveDirectory, FileRenamePolicy fileRenamePolicy) {
    this(request, saveDirectory, -1, null, fileRenamePolicy);
  }

  public MultipartBuilder(HttpRequest request, String saveDirectory) {
    this(request, saveDirectory, -1);
  }

  public MultipartBuilder(HttpRequest request) {
    this(request, null);
  }

  public Hashtable<String, UploadedFile> getFiles() {

    if (multipartRequest == null) {
      File saveDir = new File(request.getRealPath("/") + saveDirectory);
      if (!saveDir.exists()) {
        if (!saveDir.mkdirs()) {
          throw new RuntimeException("Directory " + saveDirectory + " not exists and can not create directory.");
        }
      }

      try {
        multipartRequest = new MultipartRequest(request, saveDir, maxPostSize, encoding, fileRenamePolicy);
        uploadedFiles = multipartRequest.getFiles();
      } catch (IOException e) {
        throw new WebException("Could not init multipartRequest for upload file.");
      }
    }
    return uploadedFiles;
  }
}
