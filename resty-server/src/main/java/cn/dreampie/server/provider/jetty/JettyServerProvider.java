package cn.dreampie.server.provider.jetty;

import cn.dreampie.common.Constant;
import cn.dreampie.common.util.Lister;
import cn.dreampie.log.Logger;
import cn.dreampie.server.ReloadObserver;
import cn.dreampie.server.ReloadRunnable;
import cn.dreampie.server.RestyServer;
import cn.dreampie.server.ServerException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by Dreampie on 16/9/6.
 */
public class JettyServerProvider extends RestyServer {

  private static final Logger logger = Logger.getLogger(JettyServerProvider.class);

  private Server server;
  private WebAppContext webAppContext;

  protected void init() throws Exception {

    InputStream jettyInputStream = classLoader.getResourceAsStream("jetty-server.xml");
    if (jettyInputStream != null) {
      server = (Server) new XmlConfiguration(jettyInputStream).configure();
    } else {
      server = new Server(port);
    }
    server = new Server(port);
    webAppContext = new WebAppContext();

    webAppContext.setParentLoaderPriority(true);
    webAppContext.setThrowUnavailableOnStartupException(true);

    classPath = classLoader.getResource(".").getPath();
    rootPath = new File(classPath).getParentFile().getParentFile().getCanonicalFile().getAbsolutePath() + "/";

    File webappDir = new File(rootPath + resourceBase);
    if (!webappDir.exists() || !webappDir.isDirectory()) {
      throw new IllegalArgumentException("Could not found webapp directory or it is not directory.");
    }
    String webappUrl = webappDir.getAbsolutePath();
    webXmlPath = webappUrl + "/WEB-INF/web.xml";
    webAppContext.setDescriptor(webXmlPath);
    webAppContext.setContextPath(contextPath);
    webAppContext.setResourceBase(webappUrl);

    Enumeration<URL> staticUrls = classLoader.getResources("META-INF/resources");

    URL staticURL;

    List<String> resourceUrls = Lister.of();
    resourceUrls.add(webappUrl);
    while (staticUrls.hasMoreElements()) {
      staticURL = staticUrls.nextElement();
      if (staticURL != null) {
        resourceUrls.add(staticURL.toExternalForm());
      }
    }
    if (resourceUrls.size() > 0) {
      webAppContext.setBaseResource(new ResourceCollection(resourceUrls.toArray(new String[resourceUrls.size()])));
    }

    webAppContext.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");
    webAppContext.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");

    SessionManager sessionManager = webAppContext.getSessionHandler().getSessionManager();
    sessionManager.setSessionIdPathParameterName(null);

    server.setHandler(webAppContext);
  }

  public void start() throws Exception {
    if (!isBuild) {
      throw new ServerException("You must build it before start");
    }

    if (Constant.devEnable) {
      reloadRunnable = new ReloadRunnable(this);
      reloadObserver = new ReloadObserver(reloadRunnable, this);
      reloadRunnable.addObserver(reloadObserver);

      watchThread = new Thread(reloadRunnable, "RestyServer-Watcher");//启动文件监控线程
      watchThread.start();
    }
    server.start();
    server.join();
  }

  public void stop() throws Exception {
    if (Constant.devEnable) {
      if (!watchThread.isInterrupted()) {
        watchThread.interrupt();
      }
    }
    webAppContext.stop();
    server.stop();
  }

  public void destroy() throws Exception {
    webAppContext.destroy();
    server.destroy();
  }

  public void restartWebApp() throws Exception {
    webAppContext.stop();
    logger.info("JettyServer restart...");
    webAppContext.start();
  }
}
