package cn.dreampie.server;

/**
 * Created by ice on 14-12-19.
 */
public abstract class RestyServer {

  public String host = "localhost";
  public int port = 8080;
  protected boolean isBuild = false;

  public String contextPath = "/";
  public String resourceBase = "src/main/webapp";

  public boolean useHttpSession = false;

  public RestyServer setContextPath(String contextPath) {
    this.contextPath = contextPath;
    return this;
  }

  public void setResourceBase(String resourceBase) {
    this.resourceBase = resourceBase;
  }

  public void setUseHttpSession(boolean useHttpSession) {
    this.useHttpSession = useHttpSession;
  }

  public RestyServer build() {
    this.build(port, host);
    return this;
  }

  public RestyServer build(int port) {
    this.build(port, host);
    return this;
  }

  public RestyServer build(int port, String host) {
    this.port = port;
    this.host = host;
    this.isBuild = true;
    return this;
  }


  public abstract void start() throws Exception;

  public abstract void stop() throws Exception;

}
