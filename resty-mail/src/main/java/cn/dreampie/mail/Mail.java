package cn.dreampie.mail;

/**
 * Created by wangrenhui on 14-5-6.
 */
public class Mail {
  private String charset;
  private String host;
  private String sslport;
  private int timeout;
  private int connectout;
  private String port;
  private boolean ssl;
  private boolean tls;
  private boolean debug;
  private String user;
  private String password;
  private String name;
  private String from;

  public Mail(String charset, String host, String sslport, int timeout, int connectout, String port, boolean ssl, boolean tls, boolean debug, String user, String password, String name, String from) {
    this.charset = charset;
    this.host = host;
    this.sslport = sslport;
    this.timeout = timeout;
    this.connectout = connectout;
    this.port = port;
    this.ssl = ssl;
    this.tls = tls;
    this.debug = debug;
    this.user = user;
    this.password = password;
    this.name = name;
    this.from = from;
  }

  public String getCharset() {
    return charset;
  }

  public int getTimeout() {
    return timeout;
  }

  public int getConnectout() {
    return connectout;
  }

  public String getHost() {
    return host;
  }

  public String getSslport() {
    return sslport;
  }

  public String getPort() {
    return port;
  }

  public boolean isSsl() {
    return ssl;
  }

  public String getUser() {
    return user;
  }

  public String getPassword() {
    return password;
  }

  public String getName() {
    return name;
  }

  public String getFrom() {
    return from;
  }

  public boolean isTls() {
    return tls;
  }

  public boolean isDebug() {
    return debug;
  }

}
