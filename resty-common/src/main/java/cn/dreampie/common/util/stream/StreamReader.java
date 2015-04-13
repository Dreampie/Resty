package cn.dreampie.common.util.stream;

import cn.dreampie.common.Constant;
import cn.dreampie.log.Logger;

import java.io.*;

/**
 * Created by ice on 15-1-28.
 */
public class StreamReader {

  private static final Logger logger = Logger.getLogger(StreamReader.class);

  public static String readString(InputStream is) throws IOException {
    return readString(is, Constant.encoding);
  }

  public static String readString(InputStream is, String encoding) throws IOException {
    BufferedReader rd = new BufferedReader(new InputStreamReader(is, encoding));

    StringBuilder response = new StringBuilder();
    String line;
    while ((line = rd.readLine()) != null) {
      response.append(line);
    }
    String result = response.toString();
    logger.debug("Read object in response is: %s", result);
    return result;
  }


  public static String readFile(InputStream is, int contentLength, String writeFile) throws IOException {
    //判断文件目录是否存在 如果不存在 创建
    File file = Filer.mkDirs(writeFile);

    if (file.exists()) {
      if (contentLength == 0) {
        logger.warn("File download was complete, don't download " + file.getPath());
        return file.getPath();
      }
      long start = file.length();
      //必须要使用
      RandomAccessFile out = new RandomAccessFile(file, "rw");
      out.seek(start);
      byte[] buffer = new byte[1024];
      int len = -1;
      while ((len = is.read(buffer)) != -1) {
        out.write(buffer, 0, len);
      }
      out.close();
    } else {
      //获取一个写入文件流对象
      OutputStream out = new FileOutputStream(file);
      //创建一个4*1024大小的字节数组，作为循环读取字节流的临时存储空

      byte buffer[] = new byte[4 * 1024];
      int len = -1;
      //循环读取下载的文件到buffer对象数组中
      while ((len = is.read(buffer)) != -1) {
        //把文件流写入到文件
        out.write(buffer, 0, len);
      }
      out.close();
    }
    return file.getPath();
  }

}
