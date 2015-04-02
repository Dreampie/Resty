package cn.dreampie.route.core.multipart;

import cn.dreampie.common.Constant;
import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.exception.WebException;
import cn.dreampie.upload.MultipartRequest;
import cn.dreampie.upload.multipart.DefaultFileRenamePolicy;
import cn.dreampie.upload.multipart.FileRenamePolicy;

import java.io.File;
import java.io.IOException;

/**
 * Created by ice on 15-1-6.
 */
public class MultipartBuilder {
  private String saveDirectory = Constant.uploadDirectory;
  private int maxPostSize = Constant.uploadMaxSize;
  private String[] uploadDenieds = Constant.uploadDenieds;
  private String encoding = Constant.encoding;
  private FileRenamePolicy fileRenamePolicy = new DefaultFileRenamePolicy();

  public MultipartBuilder() {
  }

  public MultipartBuilder(String saveDirectory, int maxPostSize, String encoding, String[] uploadDenieds) {
    if (saveDirectory != null && !"".equals(saveDirectory)) {
      if (saveDirectory.startsWith("/"))
        this.saveDirectory = saveDirectory;
      else
        this.saveDirectory = saveDirectory;
    }
    if (maxPostSize > 0)
      this.maxPostSize = maxPostSize;
    if (encoding != null && !"".equals(encoding))
      this.encoding = encoding;
    if (uploadDenieds != null && uploadDenieds.length > 0) {
      this.uploadDenieds = uploadDenieds;
    }
  }

  public MultipartParam readMultipart(HttpRequest request) {
    if (request == null)
      throw new IllegalArgumentException("Could not found httpRequest for multipartRequest.");

    File saveDir = new File(request.getRealPath("/") + saveDirectory);
    if (!saveDir.exists()) {
      if (!saveDir.mkdirs()) {
        throw new WebException("Directory " + saveDirectory + " not exists and can not create directory.");
      }
    }

    MultipartParam multipartParam = null;
    try {
      MultipartRequest multipartRequest = new MultipartRequest(request, saveDir, maxPostSize, encoding, fileRenamePolicy, uploadDenieds);
      multipartParam = new MultipartParam(multipartRequest.getFiles(), multipartRequest.getParameters());
    } catch (IOException e) {
      throw new WebException(e.getMessage());
    }
    return multipartParam;
  }
}
