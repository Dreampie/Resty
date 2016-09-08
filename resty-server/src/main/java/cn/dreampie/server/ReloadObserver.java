package cn.dreampie.server;

import cn.dreampie.log.Logger;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Dreampie on 16/9/7.
 */
public class ReloadObserver implements Observer {

  private static final Logger logger = Logger.getLogger(ReloadObserver.class);

  private ReloadRunnable reloadRunnable;
  private RestyServer server;

  public ReloadObserver(ReloadRunnable reloadRunnable, RestyServer server) {
    this.reloadRunnable = reloadRunnable;
    this.server = server;
  }

  public void update(Observable o, Object arg) {
    reloadRunnable.addObserver(this);
    server.watchThread = new Thread(reloadRunnable);
    server.watchThread.start();
    logger.error("ReloadObserver is start.");
  }
}