package cn.dreampie.common.http;

import cn.dreampie.common.Response;
import cn.dreampie.common.http.result.HttpStatus;
import cn.dreampie.common.util.HttpTyper;
import cn.dreampie.common.util.Joiner;
import cn.dreampie.log.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Date: 1/3/14
 * Time: 20:46
 */
public abstract class AbstractResponse<R> implements Response {
  private static final Logger logger = Logger.getLogger(AbstractResponse.class);

  private final Class<R> responseClass;
  private final R response;
  // used to store headers set to be able to return them in getHeader()
  private final Map<String, String> headers = new LinkedHashMap<String, String>();
  private HttpStatus status = HttpStatus.OK;
  private Charset charset;
  private PrintWriter writer;
  private OutputStream outputStream;
  private boolean closed;

  protected AbstractResponse(Class<R> responseClass, R response) {
    this.responseClass = responseClass;
    this.response = response;
  }


  public HttpStatus getStatus() {
    return status;
  }


  public Response setStatus(HttpStatus httpStatus) {
    this.status = httpStatus;
    doSetStatus(httpStatus);
    return this;
  }


  public Response setContentType(String s) {
    if (HttpTyper.isTextContentType(s)) {
      String cs = HttpTyper.charsetFromContentType(s);
      if (cs == null) {
        s += ";charset=UTF-8";
        charset = Encoding.UTF_8;
      } else {
        charset = Charset.forName(cs);
      }
    }
    setHeader("Content-Type", s);
    return this;
  }

  public Charset getCharset() {
    return charset;
  }


  public PrintWriter getWriter() throws IOException {
    if (writer != null) {
      return writer;
    }

    if (charset == null) {
      logger.warn("No charset defined while getting writer to write http response." +
          " Make sure you call setContentType() before calling getWriter(). Using UTF-8 charset.");
      charset = Encoding.UTF_8;
    }
    return writer = new PrintWriter(new OutputStreamWriter(doGetOutputStream(), charset), true);
  }


  public OutputStream getOutputStream() throws IOException {
    if (outputStream != null) {
      return outputStream;
    }
    return outputStream = doGetOutputStream();
  }


  public void close() throws IOException {
    if (isClosed()) {
      return;
    }
    try {
      if (writer != null) {
        writer.close();
      }
      if (outputStream != null) {
        outputStream.close();
      }
    } finally {
      closed = true;
    }
  }


  public boolean isClosed() {
    return closed;
  }


  public Response addCookie(String name, String value) {
    addCookie(name, value, -1);
    return this;
  }


  public final Response setHeader(String name, String value) {
    doSetHeader(name, value);
    headers.put(name.toLowerCase(Locale.ENGLISH), value);
    return this;
  }

  public final Response addHeader(String headerName, String header) {
    doAddHeader(headerName, header);
    headers.put(headerName.toLowerCase(Locale.ENGLISH), Joiner.on(",").join(header, header));
    return this;
  }

  protected abstract void doSetHeader(String headerName, String header);

  protected abstract void doAddHeader(String headerName, String header);

  public String getHeader(String value) {
    return headers.get(value.toLowerCase(Locale.ENGLISH));
  }


  public String toString() {
    return "[Resty response] " + status;
  }


  public <T> T unwrap(Class<T> clazz) {
    if (clazz == this.responseClass) {
      return (T) response;
    }
    throw new IllegalArgumentException("Underlying implementation is " + this.responseClass.getName()
        + ", not " + clazz.getName());
  }

  protected abstract OutputStream doGetOutputStream() throws IOException;

  protected abstract void doSetStatus(HttpStatus httpStatus);
}
