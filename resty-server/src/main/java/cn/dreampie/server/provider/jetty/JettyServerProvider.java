package cn.dreampie.server.provider.jetty;

import cn.dreampie.common.util.Lister;
import cn.dreampie.log.Logger;
import cn.dreampie.server.RestyServer;
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

  public void start() throws Exception {

    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    InputStream jettyInputStream = classLoader.getResourceAsStream("jetty-server.xml");
    if (jettyInputStream != null) {
      server = (Server) new XmlConfiguration(jettyInputStream).configure();
    } else {
      server = new Server(port);
    }
    Server server = new Server(port);
    WebAppContext webAppContext = new WebAppContext();

    webAppContext.setParentLoaderPriority(true);
    webAppContext.setThrowUnavailableOnStartupException(true);
    File webappDir = new File(new File(classLoader.getResource(".").toURI()).getParentFile().getParentFile().getCanonicalFile().getAbsolutePath() + "/" + resourceBase);
    if (!webappDir.exists() || !webappDir.isDirectory()) {
      throw new IllegalArgumentException("Could not found webapp directory or it is not directory.");
    }
    String webappUrl = webappDir.getAbsolutePath();
    webAppContext.setDescriptor(webappUrl + "WEB-INF/web.xml");
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
    server.start();
    server.join();
  }

  public void stop() throws Exception {
    server.stop();
  }
}
