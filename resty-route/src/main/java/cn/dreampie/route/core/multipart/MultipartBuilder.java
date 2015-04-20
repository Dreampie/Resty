package cn.dreampie.route.core.multipart;

import cn.dreampie.common.Constant;
import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.exception.WebException;
import cn.dreampie.common.util.stream.FileRenamer;
import cn.dreampie.log.Logger;
import cn.dreampie.upload.MultipartRequest;

import java.io.File;
import java.io.IOException;

/**
 * Created by ice on 15-1-6.
 */
public class MultipartBuilder {
  private static final Logger logger = Logger.getLogger(MultipartBuilder.class);

  private String saveDirectory = Constant.uploadDirectory;
  private int maxPostSize = Constant.uploadMaxSize;
  private String[] uploadAllows;
  private String encoding = Constant.encoding;
  private boolean overwrite = false;
  private FileRenamer renamer = FileRenamer.RENAMER;

  public MultipartBuilder() {
  }

  public MultipartBuilder(String saveDirectory, boolean overwrite, Class<? extends FileRenamer> renamerClass, int maxPostSize, String encoding, String[] uploadAllows) {
    if (saveDirectory != null && !"".equals(saveDirectory)) {
      this.saveDirectory = saveDirectory;
    }
    this.overwrite = overwrite;
    if (renamer == null || renamerClass != renamer.getClass()) {
      try {
        renamer = renamerClass.newInstance();
      } catch (InstantiationException e) {
        logger.error("Could not init FileRenamer Class.", e);
      } catch (IllegalAccessException e) {
        logger.error("Could not access FileRenamer Class.", e);
      }
    }
    if (maxPostSize > 0) {
      this.maxPostSize = maxPostSize;
    }
    if (encoding != null && !"".equals(encoding)) {
      this.encoding = encoding;
    }
    this.uploadAllows = uploadAllows;
  }

  public MultipartParam readMultipart(HttpRequest request) {
    if (request == null)
      throw new IllegalArgumentException("Could not found httpRequest for multipartRequest.");

    File saveDir = new File(saveDirectory);
    if (!saveDir.exists()) {
      if (!saveDirectory.startsWith("/")) {
        saveDirectory = "/" + saveDirectory;
      }
      saveDir = new File(request.getRealPath("/") + saveDirectory);
    }

    if (!saveDir.exists()) {
      if (!saveDir.mkdirs()) {
        throw new WebException("Directory " + saveDirectory + " not exists and can not create directory.");
      }
    }

    MultipartParam multipartParam = null;
    FileRenamer fileRenamer = null;
    //同名文件直接覆盖
    if (!overwrite && renamer != null) {
      fileRenamer = renamer;
    }

    try {
      MultipartRequest multipartRequest = new MultipartRequest(request, saveDir, maxPostSize, encoding, fileRenamer, uploadAllows, Constant.uploadDenieds);
      multipartParam = new MultipartParam(multipartRequest.getFiles(), multipartRequest.getParams());
    } catch (IOException e) {
      throw new WebException(e.getMessage());
    }
    return multipartParam;
  }
}
