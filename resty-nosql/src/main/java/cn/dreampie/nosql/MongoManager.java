package cn.dreampie.nosql;

import cn.dreampie.orm.cache.CacheEvent;
import cn.dreampie.orm.cache.CacheManager;
import cn.dreampie.log.Logger;
import com.mongodb.*;

import java.net.InetAddress;
import java.util.List;

/**
 * MongoManager  目前因没有好的设计或者对关系型数据库影响较大  mongo等nosql 目前还没有良好的设计 如果你有好的设计 欢迎交流
 */
public class MongoManager extends CacheManager {
  private static final Logger logger = Logger.getLogger(MongoManager.class);
  private final MongoClient cacheManager;

  public MongoManager(InetAddress addr) {
    this(new MongoClient(new ServerAddress(addr)));
  }

  public MongoManager(InetAddress addr, int port) {
    this(new MongoClient(new ServerAddress(addr, port)));
  }

  public MongoManager(ServerAddress serverAddress) {
    this(new MongoClient(serverAddress));
  }

  public MongoManager(ServerAddress addr, List<MongoCredential> credentialsList) {
    this(new MongoClient(addr, credentialsList, new MongoClientOptions.Builder().build()));
  }

  public MongoManager(MongoClient cacheManager) {
    this.cacheManager = cacheManager;
  }


  public Object getCache(String group, String key) {
    try {
      DBCollection c = cacheManager.getDB(group).getCollection(key);
      return c.find().next().get(key);
    } catch (Exception e) {
      logger.warn("%s", e, e);
      return null;
    }
  }

  public void addCache(String group, String key, Object cache) {
    cacheManager.getDB(group).getCollection(key).insert(new BasicDBObject(key, cache));
  }

  public void removeCache(String group, String key) {
    cacheManager.getDB(group).getCollection(key).remove(new BasicDBObject());
  }

  public void doFlush(CacheEvent event) {

    if (event.getType().equals(CacheEvent.CacheEventType.ALL)) {
      List<String> dbNames = cacheManager.getDatabaseNames();
      for (String dbName : dbNames) {
        cacheManager.dropDatabase(dbName);
      }
    } else if (event.getType().equals(CacheEvent.CacheEventType.GROUP)) {
      cacheManager.dropDatabase(event.getGroup());
    }
  }

  public DB getDB(String group) {
    return cacheManager.getDB(group);
  }

  public DBCollection getCollection(String group, String key) {
    return cacheManager.getDB(group).getCollection(key);
  }
}
