package cn.dreampie.server;

/**
 * Created by ice on 14-12-19.
 */
public abstract class RestyServer {

  protected String host = "localhost";
  protected int port = 8080;
  protected boolean isBuild = false;

  protected String contextPath = "/";
  protected String resourceBase = "src/main/webapp";

  protected boolean useHttpSession = false;
  protected String rootPath;
  protected String classPath;
  protected String webXmlPath;

  protected ReloadRunnable reloadRunnable;
  protected ReloadObserver reloadObserver;
  protected Thread watchThread;

  protected ClassLoader classLoader = RestyServer.class.getClassLoader();
  protected StackTraceElement stack;
  protected String mainFile;

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

  public RestyServer build() throws Exception {
    this.build(port, host);
    return this;
  }

  public RestyServer build(int port) throws Exception {
    this.build(port, host);
    return this;
  }

  public RestyServer build(int port, String host) throws Exception {
    this.port = port;
    this.host = host;
    this.isBuild = true;
    init();

    StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
    stack = stacks[stacks.length - 1];
    String[] names = stack.getFileName().split("\\.");
    mainFile = stack.getClassName().replaceAll("\\.", "/") + "." + names[1];
    return this;
  }

  protected abstract void init() throws Exception;

  public abstract void start() throws Exception;

  public abstract void stop() throws Exception;

  public abstract void destroy() throws Exception;

  public abstract void restartWebApp() throws Exception;
}
