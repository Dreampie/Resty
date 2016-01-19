package cn.dreampie.route.render;

import cn.dreampie.common.Render;
import cn.dreampie.common.http.ContentType;
import cn.dreampie.common.http.HttpRequest;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.common.http.exception.WebException;
import cn.dreampie.common.http.result.HttpStatus;
import cn.dreampie.log.Logger;

import java.io.*;

/**
 * Created by wangrenhui on 15/1/4.
 */
public class FileRender extends Render {
  private static final Logger logger = Logger.getLogger(FileRender.class);

  public void render(HttpRequest request, HttpResponse response, Object out) {
    if (out != null) {
      File file = null;
      BufferedInputStream bis = null;
      if (out instanceof String) {
        file = new File((String) out);
      } else if (out instanceof File) {
        file = (File) out;
      }

      if (file == null) {
        throw new WebException(HttpStatus.NOT_FOUND, "File not support '" + out + "'.");
      } else if (!file.exists()) {
        throw new WebException(HttpStatus.NOT_FOUND, "File not found '" + file.getName() + "'.");
      } else {
        try {
          long p = 0L;
          long toLength = 0L;
          long contentLength = 0L;
          int rangeSwitch = 0; // 0,从头开始的全文下载；1,从某字节开始的下载（bytes=27000-）；2,从某字节开始到某字节结束的下载（bytes=27000-39000）
          long fileLength;
          String rangBytes = "";
          fileLength = file.length();

          // get file content
          InputStream ins = new FileInputStream(file);
          bis = new BufferedInputStream(ins);

          // tell the client to allow accept-ranges
          response.reset();
          response.setHeader("Accept-Ranges", "bytes");

          // client requests a file block download start byte
          String range = request.getHeader("Range");
          if (range != null && range.trim().length() > 0 && !"null".equals(range)) {
            response.setStatus(HttpStatus.PARTIAL_CONTENT);
            rangBytes = range.replaceAll("bytes=", "");
            if (rangBytes.endsWith("-")) {  // bytes=270000-
              rangeSwitch = 1;
              p = Long.parseLong(rangBytes.substring(0, rangBytes.indexOf("-")));
              contentLength = fileLength - p;  // 客户端请求的是270000之后的字节（包括bytes下标索引为270000的字节）
            } else { // bytes=270000-320000
              rangeSwitch = 2;
              String temp1 = rangBytes.substring(0, rangBytes.indexOf("-"));
              String temp2 = rangBytes.substring(rangBytes.indexOf("-") + 1, rangBytes.length());
              p = Long.parseLong(temp1);
              toLength = Long.parseLong(temp2);
              contentLength = toLength - p + 1; // 客户端请求的是 270000-320000 之间的字节
            }
          } else {
            contentLength = fileLength;
          }

          // 如果设设置了Content-Length，则客户端会自动进行多线程下载。如果不希望支持多线程，则不要设置这个参数。
          // Content-Length: [文件的总大小] - [客户端请求的下载的文件块的开始字节]
          response.setHeader("Content-Length", Long.toString(contentLength));

          // 断点开始
          // 响应的格式是:
          // Content-Range: bytes [文件块的开始字节]-[文件的总大小 - 1]/[文件的总大小]
          if (rangeSwitch == 1) {
            String contentRange = "bytes " + Long.toString(p) + "-" + Long.toString(fileLength - 1) + "/" + Long.toString(fileLength);
            response.setHeader("Content-Range", contentRange);
            bis.skip(p);
          } else if (rangeSwitch == 2) {
            String contentRange = range.replace("=", " ") + "/" + Long.toString(fileLength);
            response.setHeader("Content-Range", contentRange);
            bis.skip(p);
          } else {
            String contentRange = "bytes " + "0-" + (fileLength - 1) + "/" + fileLength;
            response.setHeader("Content-Range", contentRange);
          }

          String fileName = file.getName();
          response.setContentType(ContentType.FILE.value());
          response.addHeader("Content-Disposition", "attachment;filename=" + fileName);

          OutputStream outputStream = response.getOutputStream();
          int n = 0;
          long readLength = 0;
          int bsize = 1024;
          byte[] bytes = new byte[bsize];
          if (rangeSwitch == 2) {
            // 针对 bytes=27000-39000 的请求，从27000开始写数据
            while (readLength <= contentLength - bsize) {
              n = bis.read(bytes);
              readLength += n;
              outputStream.write(bytes, 0, n);
            }
            if (readLength <= contentLength) {
              n = bis.read(bytes, 0, (int) (contentLength - readLength));
              outputStream.write(bytes, 0, n);
            }
          } else {
            while ((n = bis.read(bytes)) != -1) {
              outputStream.write(bytes, 0, n);
            }
          }
          bis.close();
        } catch (IOException ie) {
          // 忽略 ClientAbortException 之类的异常
        } catch (Exception e) {
          throw new WebException(e.getMessage());
        }
      }
    }
  }
}
