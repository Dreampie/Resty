package cn.dreampie.server;

import cn.dreampie.common.util.scan.FileScaner;
import cn.dreampie.log.Logger;

import java.io.File;
import java.util.*;

/**
 * Created by Dreampie on 16/9/7.
 */
public class ReloadRunnable extends Observable implements Runnable {

  private static final Logger logger = Logger.getLogger(ReloadRunnable.class);

  public static final int SCAN_INTERVAL_DEFAULT = 1000;
  public static final int RELOAD_INTERVAL_DEFAULT = 1000;
  private int scanInterval;
  private int restartInterval;
  private Set<File> files;
  private Map<String, Long> fileModifieds = new HashMap<String, Long>();
  private long lastErrorModified = 0;
  private RestyServer server;
  private FileScaner fileScaner;
  private String[] includeFiles;

  public ReloadRunnable(RestyServer server) {
    this(SCAN_INTERVAL_DEFAULT, RELOAD_INTERVAL_DEFAULT, server);
  }

  public ReloadRunnable(int scanInterval, int restartInterval, RestyServer server) {
    this.scanInterval = scanInterval;
    this.restartInterval = restartInterval;
    this.server = server;

    this.includeFiles = new String[]{server.classPath, server.webXmlPath, server.rootPath + "pom.xml"};
    this.fileScaner = FileScaner.of().isAbsolutePath(true);
    this.files = fileScaner.include(includeFiles).scanToFile();
    for (File file : files) {
      fileModifieds.put(file.getAbsolutePath(), file.lastModified());
    }
  }

  // 此方法一经调用，等待reloadInterval时间之后可以通知观察者，在本例中是通知监听线程
  public void doNotify() {
    logger.error("ReloadRunnable is dead.");
    try {
      if (!Thread.currentThread().isInterrupted()) {
        Thread.sleep(restartInterval);
      }
    } catch (InterruptedException e) {
      logger.error(e.getMessage(), e);
    }
    super.setChanged();
    notifyObservers();
  }


  public void run() {
    try {
      //执行文件对比 并重启server
      Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
      while (!Thread.currentThread().isInterrupted()) {
        Thread.sleep(scanInterval);
        this.files = this.fileScaner.include(includeFiles).scanToFile();

        Iterator<File> fileIterator = files.iterator();
        while (fileIterator.hasNext()) {
          File file = fileIterator.next();
          if (file.exists()) {
            long fileLastModified = 0;
            if (fileModifieds.containsKey(file.getAbsolutePath())) {
              fileLastModified = fileModifieds.get(file.getAbsolutePath());
            } else {
              fileModifieds.put(file.getAbsolutePath(), file.lastModified());
            }

            //文件不存在  或者 当前文件更新时间变化
            if (file.lastModified() > fileLastModified && lastErrorModified < file.lastModified()) {
              lastErrorModified = new Date().getTime();

              //启动的Main执行线程
              if (file.getAbsolutePath().endsWith(server.mainFile)) {
                logger.warn("Could not restart 'main' thread, you must rerun this application to enable this change.");
              } else {
                fileModifieds.put(file.getAbsolutePath(), file.lastModified());//reset last modify
                logger.info("File '" + file.getAbsolutePath() + "' modified at '" + new Date(file.lastModified()) + "'.");
                server.restartWebApp();
              }
            }
          } else {
            //删除不存在的文件
            fileModifieds.remove(file.getAbsolutePath());
            server.restartWebApp();
          }
        }
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      doNotify();
    }
  }
}
